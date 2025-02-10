package com.android.achievix.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.achievix.Model.AppBlockModell
import com.android.achievix.R

class AppBlockAdapterr(private var appList: List<AppBlockModell>) :
    RecyclerView.Adapter<AppBlockAdapterr.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int, isChecked: Boolean)
    }

    private var onItemClickListener: OnItemClickListener? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.app_name_block)
        val appIcon: ImageView = view.findViewById(R.id.app_icon_block)
        val blocked: ImageView = view.findViewById(R.id.app_action_block)
        val checkbox: CheckBox = view.findViewById(R.id.app_checkbox_block)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_app_blockk, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appInfo = appList[position]
        holder.appName.text = appInfo.appName
        holder.appIcon.setImageResource(appInfo.iconResId)  // Set image using resource ID
        holder.blocked.setImageResource(if (appInfo.blocked) R.drawable.lock_icon_red else R.drawable.lock_icon_grey)
        holder.checkbox.isChecked = appInfo.isChecked

        holder.checkbox.setOnClickListener {
            appInfo.isChecked = holder.checkbox.isChecked
            onItemClickListener?.onItemClick(holder.adapterPosition, holder.checkbox.isChecked)
        }

        holder.itemView.setOnClickListener {
            holder.checkbox.isChecked = !holder.checkbox.isChecked
            appInfo.isChecked = holder.checkbox.isChecked
            onItemClickListener?.onItemClick(holder.adapterPosition, holder.checkbox.isChecked)
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateListBlock(newList: List<AppBlockModell>) {
        appList = newList
        notifyDataSetChanged()
    }

    private fun convertToHrsMins(millis: Long): String {
        val hours = millis / 3600000
        val minutes = (millis % 3600000) / 60000
        return "${hours}h ${minutes}m"
    }

    override fun getItemCount() = appList.size

    fun getItemAt(position: Int): AppBlockModell {
        return appList[position]
    }

    fun getSelectedApps(): List<AppBlockModell> {
        return appList.filter { it.isChecked }
    }
}
