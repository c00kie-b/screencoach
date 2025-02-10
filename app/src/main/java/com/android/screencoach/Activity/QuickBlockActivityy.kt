package com.android.achievix.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.android.achievix.Database.BlockDatabasee
import com.android.achievix.R
import java.util.Calendar

class QuickBlockActivityy : AppCompatActivity() {
    private lateinit var launchSwitch: SwitchCompat
    private lateinit var notiSwitch: SwitchCompat
    private lateinit var untilTimePicker: TimePicker
    private lateinit var saveButton: Button
    private lateinit var textEditText: EditText
    private lateinit var text: TextView
    private lateinit var blockDatabasee: BlockDatabasee

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_blockk)

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
        untilTimePicker = findViewById(R.id.time_picker_quick_block)
        saveButton = findViewById(R.id.quick_block_button)
        textEditText = findViewById(R.id.quick_block_text)
        text = findViewById(R.id.quick_block_launch_text)

        blockDatabasee = BlockDatabasee(this)

        launchSwitch.isChecked = true
        notiSwitch.isChecked = true
    }

    @SuppressLint("SetTextI18n")
    private fun attachListeners(name: String?, packageName: String?, type: String?) {
        if (type == "web" || type == "key") {
            text.text = "Site Launch"
        }

        saveButton.setOnClickListener {
            val untilHours = untilTimePicker.hour
            val untilMins = untilTimePicker.minute

            val motivationalText = textEditText.text.toString().let {
                it.ifEmpty {
                    null
                } ?: it
            }

            val launch = launchSwitch.isChecked
            val noti = notiSwitch.isChecked

            if (untilHours == 0 && untilMins == 0) {
                Toast.makeText(this, "Please enter a valid time", Toast.LENGTH_SHORT).show()
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
                        "Quick Block",
                        "$untilHours $untilMins",
                        getCurrentDay().toString(),
                        null,
                        true,
                        motivationalText
                    )
                }

                "web" -> {
                    blockDatabasee.addRecord(
                        name,
                        null,
                        "web",
                        launch,
                        noti,
                        "Quick Block",
                        "$untilHours $untilMins",
                        getCurrentDay().toString(),
                        null,
                        true,
                        motivationalText
                    )
                }

                "key" -> {
                    blockDatabasee.addRecord(
                        name,
                        null,
                        "key",
                        launch,
                        noti,
                        "Quick Block",
                        "$untilHours $untilMins",
                        getCurrentDay().toString(),
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
                        "Quick Block",
                        "$untilHours $untilMins",
                        getCurrentDay().toString(),
                        intent.getStringExtra("profileName"),
                        true,
                        motivationalText
                    )

                    blockDatabasee.addAllItemsToNewProfileSchedule(
                        launch,
                        noti,
                        intent.getStringExtra("profileName"),
                        "Quick Block",
                        "$untilHours $untilMins",
                        getCurrentDay().toString(),
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
    }

    private fun getCurrentDay(): List<String> {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> mutableListOf("Sunday")
            Calendar.MONDAY -> mutableListOf("Monday")
            Calendar.TUESDAY -> mutableListOf("Tuesday")
            Calendar.WEDNESDAY -> mutableListOf("Wednesday")
            Calendar.THURSDAY -> mutableListOf("Thursday")
            Calendar.FRIDAY -> mutableListOf("Friday")
            Calendar.SATURDAY -> mutableListOf("Saturday")
            else -> mutableListOf("")
        }
    }

    fun finish(v: View?) {
        finish()
    }
}