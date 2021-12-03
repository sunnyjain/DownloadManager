package com.sample.download_manager_app.example.listener

import android.view.View
import com.sample.download_manager_app.downloadmanager.models.Task

interface OnTaskItemClickListener {
    fun onTaskItemClick(view: View, task: Task)
}