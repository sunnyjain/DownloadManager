package com.sample.download_manager_app.downloadmanager.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sample.download_manager_app.downloadmanager.database.TaskDao
import com.sample.download_manager_app.downloadmanager.models.Task

class DownloadManagerRepository constructor(private val taskDao: TaskDao): DownloadManagerContract.Repository {

    companion object {
        @Volatile private var instance: DownloadManagerRepository? = null

        fun getInstance(taskDao: TaskDao) =
            instance ?: synchronized(this) {
                instance ?: DownloadManagerRepository(taskDao).also { instance = it }
            }
    }

    /**
     * This mutable live data is useful for operations like pause, stop, resume.
     * Consider that the user interacted with the pause button the mutable live data will set the Task and hold the
     * Task object. This livedata is observed in service class. Accordingly we then process the information.
     *
     * Interactions like Pause, Stop/cancel or resume the download are handled once. This livedata is used to
     * change/handle the state and accordingly carry out the operation.
     * */
    val taskLiveData: MutableLiveData<Task> = MutableLiveData()

    override fun getTaskList(): LiveData<MutableList<Task>> {
        return taskDao.getDownloadTasks()
    }

    override suspend fun insertTask(task: Task) {
        taskDao.addDownloadTask(task)
    }

    override suspend fun removeTask(taskIds: List<Long>) {
        taskDao.removeDownloadTask(taskIds)
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateDownloadTask(task)
    }

    override suspend fun getTaskListByState(state: Int): List<Task> {
        return taskDao.getTasksByState(state)
    }

}