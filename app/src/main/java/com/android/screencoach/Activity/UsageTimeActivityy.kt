package com.android.achievix.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.android.achievix.Database.BlockDatabasee
import com.android.achievix.R

class UsageTimeActivityy : AppCompatActivity() {
    private lateinit var launchSwitch: SwitchCompat
    private lateinit var notiSwitch: SwitchCompat
    private lateinit var monRadioButton: RadioButton
    private lateinit var tueRadioButton: RadioButton
    private lateinit var wedRadioButton: RadioButton
    private lateinit var thuRadioButton: RadioButton
    private lateinit var friRadioButton: RadioButton
    private lateinit var satRadioButton: RadioButton
    private lateinit var sunRadioButton: RadioButton
    private lateinit var hoursEditText: EditText
    private lateinit var minsEditText: EditText
    private lateinit var textEditText: EditText
    private lateinit var saveButton: Button
    private val days = mutableListOf<String>()
    private lateinit var blockDatabasee: BlockDatabasee

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usage_timee)

        val intent = intent
        val name = intent.getStringExtra("name")
        val packageName = intent.getStringExtra("packageName")
        val type = intent.getStringExtra("type")

        initializeViews()
        attachListeners(name, packageName, type)
    }

    private fun initializeViews() {
        launchSwitch = findViewById(R.id.block_app_launch_usage_time)
        notiSwitch = findViewById(R.id.block_noti_usage_time)
        monRadioButton = findViewById(R.id.monday)
        tueRadioButton = findViewById(R.id.tuesday)
        wedRadioButton = findViewById(R.id.wednesday)
        thuRadioButton = findViewById(R.id.thursday)
        friRadioButton = findViewById(R.id.friday)
        satRadioButton = findViewById(R.id.saturday)
        sunRadioButton = findViewById(R.id.sunday)
        hoursEditText = findViewById(R.id.usage_time_hrs)
        minsEditText = findViewById(R.id.usage_time_mins)
        textEditText = findViewById(R.id.usage_time_text)
        saveButton = findViewById(R.id.usage_time_block_button)

        blockDatabasee = BlockDatabasee(this)

        launchSwitch.isChecked = true
        notiSwitch.isChecked = true
    }

    @SuppressLint("SetTextI18n")
    private fun attachListeners(name: String?, packageName: String?, type: String?) {
        saveButton.setOnClickListener {
            val hours = hoursEditText.text.toString().isEmpty().let {
                if (it) {
                    0
                } else {
                    hoursEditText.text.toString().toInt()
                }
            }
            val mins = minsEditText.text.toString().isEmpty().let {
                if (it) {
                    0
                } else {
                    minsEditText.text.toString().toInt()
                }
            }

            val motivationalText = textEditText.text.toString().let {
                it.ifEmpty {
                    null
                } ?: it
            }

            val launch = launchSwitch.isChecked
            val noti = notiSwitch.isChecked

            if (hours == 0 && mins == 0) {
                Toast.makeText(this, "Please enter a valid time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (days.isEmpty()) {
                Toast.makeText(this, "Please select a day", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!launch && !noti) {
                Toast.makeText(this, "Please select at least one option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (type) {
                "app" -> {
                    blockDatabasee.addRecord(
                        name,
                        packageName,
                        "app",
                        launch,
                        noti,
                        "Usage Time",
                        "$hours $mins",
                        days.toString(),
                        null,
                        true,
                        motivationalText
                    )
                }

                "profile" -> {
                    blockDatabasee.addRecord(
                        null,
                        null,
                        null,
                        launch,
                        noti,
                        "Usage Time",
                        "$hours $mins",
                        days.toString(),
                        intent.getStringExtra("profileName"),
                        true,
                        motivationalText
                    )

                    blockDatabasee.addAllItemsToNewProfileSchedule(
                        launch,
                        noti,
                        intent.getStringExtra("profileName"),
                        "Usage Time",
                        "$hours $mins",
                        days.toString(),
                        true,
                        motivationalText
                    )
                }
            }

            Toast.makeText(this, "Schedule added", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                val intent = Intent(this, MainActivityy::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }, 1000)
        }
        setupDayCheckListeners()
    }

    private fun setupDayCheckListeners() {
        var isSundayChecked = false
        sunRadioButton.setOnClickListener {
            isSundayChecked = !isSundayChecked
            sunRadioButton.isChecked = isSundayChecked
            if (isSundayChecked) {
                days.add("Sunday")
            } else {
                days.remove("Sunday")
            }
        }

        var isMondayChecked = false
        monRadioButton.setOnClickListener {
            isMondayChecked = !isMondayChecked
            monRadioButton.isChecked = isMondayChecked
            if (isMondayChecked) {
                days.add("Monday")
            } else {
                days.remove("Monday")
            }
        }

        var isTuesdayChecked = false
        tueRadioButton.setOnClickListener {
            isTuesdayChecked = !isTuesdayChecked
            tueRadioButton.isChecked = isTuesdayChecked
            if (isTuesdayChecked) {
                days.add("Tuesday")
            } else {
                days.remove("Tuesday")
            }
        }

        var isWednesdayChecked = false
        wedRadioButton.setOnClickListener {
            isWednesdayChecked = !isWednesdayChecked
            wedRadioButton.isChecked = isWednesdayChecked
            if (isWednesdayChecked) {
                days.add("Wednesday")
            } else {
                days.remove("Wednesday")
            }
        }

        var isThursdayChecked = false
        thuRadioButton.setOnClickListener {
            isThursdayChecked = !isThursdayChecked
            thuRadioButton.isChecked = isThursdayChecked
            if (isThursdayChecked) {
                days.add("Thursday")
            } else {
                days.remove("Thursday")
            }
        }

        var isFridayChecked = false
        friRadioButton.setOnClickListener {
            isFridayChecked = !isFridayChecked
            friRadioButton.isChecked = isFridayChecked
            if (isFridayChecked) {
                days.add("Friday")
            } else {
                days.remove("Friday")
            }
        }

        var isSaturdayChecked = false
        satRadioButton.setOnClickListener {
            isSaturdayChecked = !isSaturdayChecked
            satRadioButton.isChecked = isSaturdayChecked
            if (isSaturdayChecked) {
                days.add("Saturday")
            } else {
                days.remove("Saturday")
            }
        }
    }

    fun finish(v: View?) {
        finish()
    }
}