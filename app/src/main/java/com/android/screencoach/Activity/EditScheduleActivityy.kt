package com.android.achievix.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.achievix.Adapter.ScheduleAdapterr
import com.android.achievix.Database.BlockDatabasee
import com.android.achievix.Model.ScheduleModell
import com.android.achievix.R

class EditScheduleActivityy : AppCompatActivity() {
    private lateinit var scheduleAdapter: ScheduleAdapterr
    private var scheduleModelList: MutableList<ScheduleModell> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: ImageButton
    private lateinit var doneButton: Button
    private lateinit var noSchedule: TextView
    private var blockDatabasee: BlockDatabasee = BlockDatabasee(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_schedulee)

        val intent = intent
        val name = intent.getStringExtra("name")
        val packageName = intent.getStringExtra("packageName") ?: "null"
        val type = intent.getStringExtra("type")

        initializeViews(name!!)
        initRecyclerView(name, packageName, type!!)
        attachListeners(name, packageName, type)
    }

    private fun initializeViews(text: String) {
        findViewById<TextView?>(R.id.edit_app_name).text = text
        noSchedule = findViewById(R.id.no_schedule_text)
        addButton = findViewById(R.id.add_schedule)
        doneButton = findViewById(R.id.save_edit_schedule_button)
        recyclerView = findViewById(R.id.edit_schedule_list)
    }

    private fun initRecyclerView(name: String, packageName: String, type: String) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = DefaultItemAnimator()

        when (type) {
            "app" -> {
                val result = blockDatabasee.readRecordsApp(packageName)
                for (i in result.indices) {
                    val map = result[i]
                    val schedule = ScheduleModell(
                        id = map["id"].toString(),
                        name = map["name"].toString(),
                        packageName = map["packageName"].toString(),
                        type = map["type"].toString(),
                        launch = map["launch"].toString(),
                        notification = map["notification"].toString(),
                        scheduleType = map["scheduleType"].toString(),
                        scheduleParams = map["scheduleParams"].toString(),
                        scheduleDays = map["scheduleDays"].toString(),
                        profileName = map["profileName"].toString(),
                        profileStatus = map["profileStatus"].toString(),
                        text = map["text"].toString()
                    )

                    if (schedule.profileName == "null" && schedule.type == "app") {
                        scheduleModelList.add(schedule)
                    }
                }
            }

            "web" -> {
                val result = blockDatabasee.readRecordsWeb(name)
                for (i in result.indices) {
                    val map = result[i]
                    val schedule = ScheduleModell(
                        id = map["id"].toString(),
                        name = map["name"].toString(),
                        packageName = map["packageName"].toString(),
                        type = map["type"].toString(),
                        launch = map["launch"].toString(),
                        notification = map["notification"].toString(),
                        scheduleType = map["scheduleType"].toString(),
                        scheduleParams = map["scheduleParams"].toString(),
                        scheduleDays = map["scheduleDays"].toString(),
                        profileName = map["profileName"].toString(),
                        profileStatus = map["profileStatus"].toString(),
                        text = map["text"].toString()
                    )

                    if (schedule.profileName == "null" && schedule.type == "web") {
                        scheduleModelList.add(schedule)
                    }
                }
            }

            "key" -> {
                val result = blockDatabasee.readRecordsKey(name)
                for (i in result.indices) {
                    val map = result[i]
                    val schedule = ScheduleModell(
                        id = map["id"].toString(),
                        name = map["name"].toString(),
                        packageName = map["packageName"].toString(),
                        type = map["type"].toString(),
                        launch = map["launch"].toString(),
                        notification = map["notification"].toString(),
                        scheduleType = map["scheduleType"].toString(),
                        scheduleParams = map["scheduleParams"].toString(),
                        scheduleDays = map["scheduleDays"].toString(),
                        profileName = map["profileName"].toString(),
                        profileStatus = map["profileStatus"].toString(),
                        text = map["text"].toString()
                    )

                    if (schedule.profileName == "null" && schedule.type == "key") {
                        scheduleModelList.add(schedule)
                    }
                }
            }

            "internet" -> {
                val result = blockDatabasee.readRecordsInternet(packageName)
                for (i in result.indices) {
                    val map = result[i]
                    val schedule = ScheduleModell(
                        id = map["id"].toString(),
                        name = map["name"].toString(),
                        packageName = map["packageName"].toString(),
                        type = map["type"].toString(),
                        launch = map["launch"].toString(),
                        notification = map["notification"].toString(),
                        scheduleType = map["scheduleType"].toString(),
                        scheduleParams = map["scheduleParams"].toString(),
                        scheduleDays = map["scheduleDays"].toString(),
                        profileName = map["profileName"].toString(),
                        profileStatus = map["profileStatus"].toString(),
                        text = map["text"].toString()
                    )

                    if (schedule.profileName == "null" && schedule.type == "internet") {
                        scheduleModelList.add(schedule)
                    }
                }
            }
        }

        scheduleAdapter = ScheduleAdapterr(scheduleModelList, this)
        recyclerView.adapter = scheduleAdapter
    }

    private fun attachListeners(name: String, packageName: String, type: String) {
        addButton.setOnClickListener {
            if (type != "internet") {
                val intent = Intent(this, NewScheduleActivityy::class.java).apply {
                    putExtra("name", name)
                    putExtra("packageName", packageName)
                    putExtra("type", type)
                    putExtra("caller", "editSchedule")
                }
                startActivity(intent)
            } else {
                val intent = Intent(this, BlockDataActivityy::class.java).apply {
                    putExtra("name", name)
                    putExtra("packageName", packageName)
                }
                startActivity(intent)
            }
        }

        doneButton.setOnClickListener {
            Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                val intent = Intent(this, MainActivityy::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }, 1000)
        }
    }

    fun updateNoScheduleVisibility() {
        noSchedule.visibility = if (recyclerView.adapter!!.itemCount == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun finish(v: View?) {
        finish()
    }
}