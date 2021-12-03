package com.sample.download_manager_app.example.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.sample.download_manager_app.R
import com.sample.download_manager_app.downloadmanager.models.Task
import com.sample.download_manager_app.example.listener.OnTaskItemClickListener

class TaskListAdapter(private var taskList: ArrayList<Task>,
                      private var onTaskItemClickListener: OnTaskItemClickListener):  RecyclerView.Adapter<TaskListAdapter.CustomViewHolder>()  {


    fun submitList(list: List<Task>) {
        val diffCallback = CustomDiffUtilCallback(this.taskList, list)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        this.taskList = list as ArrayList<Task>
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CustomViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_layout, parent, false))


    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(taskList[holder.adapterPosition], onTaskItemClickListener)
    }

    override fun getItemCount() = taskList.size

    class CustomViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val taskPercent = itemView.findViewById<LinearProgressIndicator>(R.id.task_percent)
        private val pauseTask = itemView.findViewById<AppCompatButton>(R.id.pause_task)
        private val resumeTask = itemView.findViewById<AppCompatButton>(R.id.resume_task)
        private val stopTask = itemView.findViewById<AppCompatButton>(R.id.stop_task)
        fun bind(task: Task, onTaskItemClickListener: OnTaskItemClickListener) {
            itemView.findViewById<TextView>(R.id.task_id).text = itemView.context
                .getString(R.string.txt_task_id, task.id.toString())
            taskPercent.progress = task.percent
            taskPercent.visibility =
                if(task.percent == 100) View.GONE else View.VISIBLE

            itemView.findViewById<TextView>(R.id.task_status).text = itemView.context
                .getString(R.string.txt_task_status, when(task.state) {
                    0 -> "Initialized"
                    1 -> "Ready"
                    2 -> "Download in Progress"
                    3 -> "Download paused"
                    4 -> "Download finished"
                    else -> "Failed"
                })

            pauseTask.setOnClickListener { onTaskItemClickListener.onTaskItemClick(it, task) }
            resumeTask.setOnClickListener { onTaskItemClickListener.onTaskItemClick(it, task) }
            stopTask.setOnClickListener { onTaskItemClickListener.onTaskItemClick(it, task) }
        }

    }

    open class CustomDiffUtilCallback(private val oldDataSet: List<Task>,
                                      private val newDataSet: List<Task>)
        : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldDataSet[oldItemPosition].id == newDataSet[newItemPosition].id

        override fun getOldListSize() = oldDataSet.size

        override fun getNewListSize() = newDataSet.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldDataSet[oldItemPosition] == newDataSet[newItemPosition]

    }
}