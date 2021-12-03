package com.sample.download_manager_app.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sample.download_manager_app.R
import com.sample.download_manager_app.downloadmanager.DownloadManager
import com.sample.download_manager_app.downloadmanager.models.Task
import com.sample.download_manager_app.example.adapter.TaskListAdapter
import com.sample.download_manager_app.example.listener.OnTaskItemClickListener

class MainActivity : AppCompatActivity(), OnTaskItemClickListener {

    lateinit var downloadManager: DownloadManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadManager = DownloadManager(this)

        findViewById<FloatingActionButton>(R.id.addUrl)
            .setOnClickListener {
                downloadManager.enqueueDownload("https://file-examples-com.github.io/uploads/2017/02/zip_9MB.zip")
            }

        val adapter = TaskListAdapter(ArrayList(), this)
        findViewById<RecyclerView>(R.id.taskList).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        downloadManager.getTaskList().observe(this, Observer {
            adapter.submitList(it)
        })
    }

    override fun onTaskItemClick(view: View, task: Task) {
        when(view.id) {
            R.id.pause_task -> {
                downloadManager.pauseDownload(task)
            }
            R.id.resume_task -> {
                downloadManager.resumeDownload(task)
            }
            R.id.stop_task -> {
                downloadManager.stopDownload(task)
            }
        }
    }
}