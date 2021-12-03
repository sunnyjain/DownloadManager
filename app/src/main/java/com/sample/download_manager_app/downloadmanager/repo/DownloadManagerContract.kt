package com.sample.download_manager_app.downloadmanager.repo

import androidx.lifecycle.LiveData
import com.sample.download_manager_app.downloadmanager.models.Task

interface DownloadManagerContract {

    interface Repository {
        fun getTaskList(): LiveData<MutableList<Task>>
        suspend fun insertTask(task: Task)
        suspend fun removeTask(taskIds: List<Long>)
        suspend fun updateTask(task: Task)
    }
}