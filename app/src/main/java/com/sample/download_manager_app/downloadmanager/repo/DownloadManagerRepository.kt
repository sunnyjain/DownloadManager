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