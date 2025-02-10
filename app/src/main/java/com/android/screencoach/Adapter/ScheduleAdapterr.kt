package com.android.achievix.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.achievix.R
import com.android.achievix.Activity.EditScheduleActivityy
import com.android.achievix.Database.BlockDatabasee
import com.android.achievix.Model.ScheduleModell
import java.text.SimpleDateFormat
import java.util.Locale

class ScheduleAdapterr(
        private var scheduleList: List<ScheduleModell>,
        private val activity: EditScheduleActivityy
) : RecyclerView.Adapter<ScheduleAdapterr.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val daysActive: TextView = view.findViewById(R.id.days_active)
        val blockCondition: TextView = view.findViewById(R.id.block_condition)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_schedule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_edit_schedulee, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scheduleInfo = scheduleList[position]

        if (scheduleInfo.scheduleDays.isNotEmpty()) {
            holder.daysActive.text = scheduleInfo.scheduleDays.filter {
                it != '[' && it != ']'
            }
        } else {
            holder.daysActive.text = "None"
        }

        val blockCondition = StringBuilder()
        when (scheduleInfo.scheduleType) {
            "Usage Time" -> {
                blockCondition.append("Usage Time: after ")
                val time = scheduleInfo.scheduleParams.split(" ")
                blockCondition.append(
                    time[0] + "." + time[1] + "hrs"
                )
            }

            "Specific Time" -> {
                blockCondition.append("Specific Time: from ")
                val time = scheduleInfo.scheduleParams.split(" ")
                blockCondition.append(refactorTime(time[0] + " " + time[1]))
                blockCondition.append(" to ")
                blockCondition.append(refactorTime(time[2] + " " + time[3]))
            }

            "Quick Block" -> {
                blockCondition.append("Quick Block: until ")
                val time = scheduleInfo.scheduleParams.split(" ")
                blockCondition.append(refactorTime(time[0] + " " + time[1]))
            }

            "Launch Count" -> {
                blockCondition.append("App Launch: after ")
                blockCondition.append(scheduleInfo.scheduleParams)
                blockCondition.append(" launches")
            }

            "Fixed Block" -> {
                blockCondition.append("Fixed Block")
            }

            "Block Data" -> {
                blockCondition.append("Block Data: after ")
                blockCondition.append(scheduleInfo.scheduleParams)
                blockCondition.append(" MB")
            }
        }
        holder.blockCondition.text = blockCondition.toString()

        holder.deleteButton.setOnClickListener {
            val blockDatabasee = BlockDatabasee(holder.itemView.context)
            blockDatabasee.deleteRecordById(scheduleInfo.id)

            scheduleList = scheduleList.filter { it.id != scheduleInfo.id }

            notifyItemRemoved(position)
            activity.updateNoScheduleVisibility()
            Toast.makeText(holder.itemView.context, "Schedule deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refactorTime(time: String): String {
        val inputFormat = SimpleDateFormat("HH mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(time)
        return if (date != null) outputFormat.format(date) else time
    }

    override fun getItemCount() = scheduleList.size
}