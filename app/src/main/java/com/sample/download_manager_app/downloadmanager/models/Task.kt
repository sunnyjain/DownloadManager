package com.sample.download_manager_app.downloadmanager.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Data class "Task" that is the going to be used store and accordingly called from the background.
 * */
@Entity(tableName = "task")
data class Task(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long,

    @ColumnInfo(name = "url")
    var url: String,

    @ColumnInfo(name = "filename")
    var filename: String,

    @ColumnInfo(name = "percent")
    var percent: Int,

    @ColumnInfo(name = "state")
    var state: Int,

    @ColumnInfo(name = "size")
    var size: Long,

    @ColumnInfo(name = "notify")
    var notify: Boolean,
)