package com.sample.download_manager_app.downloadmanager.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sample.download_manager_app.downloadmanager.models.Task

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addDownloadTask(task: Task): Long

    @Query("SELECT * FROM task order by id DESC")
    fun getDownloadTasks(): LiveData<MutableList<Task>>

    @Query("DELETE FROM task WHERE id IN (:taskIds)")
    suspend fun removeDownloadTask(taskIds: List<Long>): Void

    @Query("SELECT * FROM task WHERE state = (:state)")
    suspend fun getTasksByState(state: Int): List<Task>

    @Update
    suspend fun updateDownloadTask(task: Task): Void
}