package com.sample.download_manager_app.downloadmanager.core

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

import com.sample.download_manager_app.R
import com.sample.download_manager_app.downloadmanager.models.Task
import com.sample.download_manager_app.downloadmanager.repo.DownloadManagerRepository
import com.sample.download_manager_app.downloadmanager.utils.InjectorUtils
import com.sample.download_manager_app.downloadmanager.utils.networkutil.ConnectivityService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File

import java.util.*
import kotlin.collections.HashMap

class DownloadDataService : Service() {

    companion object {
        private const val CHANNEL_ID = "DMService1001"
        private const val CHANNEL_NAME = "Download-Data-Service"
        private const val SERVICE_ID = R.string.app_name
        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
        var IS_RUNNING = false
    }

    private lateinit var repo: DownloadManagerRepository
    private lateinit var downloadApiService: DownloadApiService

    private val test = HashMap<Long, Job>()

    override fun onCreate() {
        super.onCreate()
        repo = InjectorUtils.getDownloadManagerRepository(applicationContext)
        ConnectivityService.instance.initializeWithApplicationContext(applicationContext)
        downloadApiService = InjectorUtils.getRetrofitService().create(DownloadApiService::class.java)
        startForegroundService()
    }

    override fun onBind(p0: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_STOP_FOREGROUND_SERVICE -> {
                    stopService()
                }
            }
        }
        return START_STICKY
    }

    private fun stopService() {
        // Stop foreground service and remove the notification.
        stopForeground(true)
        // Stop the foreground service.
        stopSelf()

        IS_RUNNING = false
    }

    override fun onDestroy() {
        super.onDestroy()
        IS_RUNNING = false

    }


    private fun startForegroundService() {

        createNotificationChannel()

        val pendingIntent =
            PendingIntent.getActivity(this, 0,
                packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)

        startForeground(SERVICE_ID, getStickyNotification(builder, pendingIntent))

        IS_RUNNING = true

        //todo:check the status of processes that are in pause states and update their states to ready.

        repo.taskLiveData.observeForever {
            taskAtHand ->

            when(taskAtHand.state) {
                TaskStates.PAUSING -> {
                    pauseDownload(taskAtHand)
                }
                TaskStates.RESUMING -> {
                    resumeDownload(taskAtHand)
                }
                TaskStates.CANCEL -> {
                    cancelDownload(taskAtHand)
                }
            }

        }

        repo.getTaskList().observeForever { taskList ->
            downloadData(taskList.filter { it.state == TaskStates.INIT })
        }
    }

    private fun resumeDownload(task: Task) {
        task.state = TaskStates.INIT
            Log.e("##DM", "TASK RESUMING")
            CoroutineScope(Dispatchers.IO).launch {
                repo.updateTask(task)
            }
    }

    private fun cancelDownload(task: Task) {
        test[task.id]?.cancel()
        test.remove(task.id)
        if(File(filesDir, task.filename).delete())
            Log.e("##DM", "TASK CANCELLED AND FILE REMOVED")
        CoroutineScope(Dispatchers.IO).launch {
            repo.removeTask(listOf(task.id))
        }
    }

    private fun pauseDownload(task: Task) {
        test[task.id]?.cancel()
        test.remove(task.id)
        Log.e("##DM", "TASK PAUSED")
        CoroutineScope(Dispatchers.IO).launch {
            repo.updateTask(task.apply { this.state = TaskStates.PAUSED })
        }
    }

    private fun downloadData(taskList: List<Task>) {
        //once they start change their state to in progress.
        taskList.forEach { task ->
            val filename = if(task.filename.isNotEmpty()) task.filename else Date().time.toString()
            val progress = if(task.percent != 0) task.percent else 0
            if(test.containsKey(task.id)) {
                return
            }
            val job = CoroutineScope(Dispatchers.IO).launch {
                downloadApiService.getUrl(task.url).downloadToFileWithProgress(
                    applicationContext,
                    filename,
                    progress
                ).collect { download ->
                    when (download) {
                        //todo: not needed
                        is Download.Initialize -> {
                            Log.e("##DM", "TASK INIT")
                            repo.updateTask(task.apply {
                                this.state = TaskStates.READY
                                this.filename = download.filename
                            })
                        }
                        is Download.Started -> {
                            Log.e("##DM", "TASK PROCESS STARTED")
                            repo.updateTask(task.apply {
                                this.state = TaskStates.DOWNLOADING
                            })
                        }
                        is Download.Progress -> {
                            repo.updateTask(task.apply {
                                this.percent = download.percent
                                this.size = download.size
                            })
                        }
                        is Download.Finished -> {
                            Log.e(
                                "##DM", "TASK FINISHED - "
                                    .plus(task.id.toString()).plus("\t")
                                    .plus(task.percent.toString()
                                        .plus("\t").plus(download.file.length()))
                            )
                            repo.updateTask(task.apply {
                                this.state = TaskStates.DOWNLOAD_FINISHED
                                this.size = download.file.length()
                                this.notify = true
                            })
                        }
                        is Download.Error -> {
                            Log.e(
                                "##DM", "TASK ERROR - "
                                    .plus(task.id.toString()).plus("\t")
                                    .plus(download.error.message)
                            )
                            repo.updateTask(task.apply {
                                this.state = TaskStates.END
                                this.notify = true
                            })
                        }
                    }
                }
            }
            test[task.id] = job
        }

    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val chan = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
            ).apply {
                lightColor = Color.BLUE
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }

            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)

        }
    }

    private fun getStickyNotification(
        builder: NotificationCompat.Builder,
        pendingIntent: PendingIntent
    ): Notification {
            builder
                .setContentText(getString(R.string.download_manager_service))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .priority = NotificationCompat.PRIORITY_LOW

        return builder.build()

    }
}