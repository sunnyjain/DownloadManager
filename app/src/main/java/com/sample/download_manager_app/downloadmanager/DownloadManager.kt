package com.sample.download_manager_app.downloadmanager

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.LiveData
import com.sample.download_manager_app.downloadmanager.core.DownloadDataService
import com.sample.download_manager_app.downloadmanager.core.TaskStates
import com.sample.download_manager_app.downloadmanager.models.Task
import com.sample.download_manager_app.downloadmanager.repo.DownloadManagerRepository
import com.sample.download_manager_app.downloadmanager.utils.InjectorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * This class is responsible for starting the enqueue url and initiate the download the process.
 * This will act as api that will be used to query download process and other things..
 * */
class DownloadManager(context: Context) {
    private var saveFolderLocation: String = ""
    private var repo: DownloadManagerRepository = InjectorUtils.getDownloadManagerRepository(context)
    private val connService: ConnectivityService by lazy {
        ConnectivityService.instance.initializeWithApplicationContext(context)
        ConnectivityService.instance
    }
    init {
        //start the foreground process.
        connService.registerNetworkCallback(object: ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.e("##DM", "NETWORK AVAILABLE")
                startForegroundService(context)
            }

            override fun onLost(network: Network) {
                if(connService.isOnline()) {
                    CoroutineScope(Dispatchers.Default).launch {
                        pauseAllDownloadingTasksOnNetworkFailure(
                            repo.getTaskListByState(TaskStates.DOWNLOADING))
                    }
                    context.stopService(Intent(context, DownloadDataService::class.java))
                    Log.e("##DM", "NETWORK LOST")
                }
                else Log.e("##DM", "NETWORK MIGHT BE CHANGED OR SOMETHING")
            }
        })
        startForegroundService(context)

    }

    fun startForegroundService(context: Context) {
        if (connService.isOnline() && !DownloadDataService.IS_RUNNING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, DownloadDataService::class.java))
            } else {
                context.startService(Intent(context, DownloadDataService::class.java))
            }
        }
    }

    /**
     * method that provides live data of the list of tasks.
     * */
    fun getTaskList(): LiveData<MutableList<Task>> {
        return repo.getTaskList()
    }

    /**
     * method to add the url to start the downloading the information from the URL.
     * @param url - the URL from which we will download the data.
     * */
    fun enqueueDownload(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if(ConnectivityService.instance.isOnline())
                repo.insertTask(Task(0, url, "",0, TaskStates.INIT,  0, false))
        }
    }

    /**
     * method to change the location of the file where you want to save the information.
     * @param location
     * */
    fun changeSaveFolderLocation(location: String) {
        this.saveFolderLocation = location
    }

    fun pauseDownload(task: Task) {
        task.state = TaskStates.PAUSING
        repo.taskLiveData.value = task
        CoroutineScope(Dispatchers.IO).launch {
            repo.updateTask(task)
        }
    }

    fun stopDownload(task: Task) {
        task.state = TaskStates.CANCEL
        repo.taskLiveData.value = task
        CoroutineScope(Dispatchers.IO).launch {
            repo.updateTask(task)
        }
    }

    fun resumeDownload(task: Task) {
        task.state = TaskStates.RESUMING
        repo.taskLiveData.value = task
        CoroutineScope(Dispatchers.IO).launch {
            repo.updateTask(task)
        }
    }


}