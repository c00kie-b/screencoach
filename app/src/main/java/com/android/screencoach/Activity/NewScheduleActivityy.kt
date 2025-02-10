package com.android.achievix.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.achievix.R

class NewScheduleActivityy : AppCompatActivity() {
    private lateinit var name: String
    private lateinit var packageName: String
    private lateinit var type: String
    private lateinit var profileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_schedulee)

        val intent = intent
        val caller = intent.getStringExtra("caller")

        if (caller != "profile") {
            name = intent.getStringExtra("name").toString()
            packageName = intent.getStringExtra("packageName").toString()
            type = intent.getStringExtra("type").toString()
            setupListeners(caller, type)
        } else {
            profileName = intent.getStringExtra("profileName")!!
            setupListeners(caller, "profile")
        }
    }

    private fun setupListeners(caller: String?, type: String?) {
        if (type == "app" || type == "profile") {
            findViewById<LinearLayout>(R.id.quick_block_button).setOnClickListener {
                startNewActivity(caller, QuickBlockActivityy::class.java)
            }
        } else {
            findViewById<LinearLayout>(R.id.quick_block_button).visibility = LinearLayout.GONE
        }

        findViewById<LinearLayout>(R.id.wait_timer_button).setOnClickListener {
            startNewActivity(caller, FixedBlockActivityy::class.java)
        }

        findViewById<LinearLayout>(R.id.location_trigger_button).setOnClickListener {
            Log.d("NewScheduleActivity", "Location trigger button clicked.")
            Toast.makeText(this, "Location trigger button clicked.", Toast.LENGTH_SHORT).show()

            try {
                startNewActivity(caller, LocationTriggerActivityy::class.java)
            } catch (e: Exception) {
                Log.e("NewScheduleActivity", "Error starting LocationTriggerActivityy", e)
            }
        }
    }

    private fun startNewActivity(caller: String?, activityClass: Class<*>) {
        Intent(this, activityClass).also {
            if (caller != "profile") {
                it.putExtra("name", name)
                it.putExtra("packageName", packageName)
                it.putExtra("type", type)
            } else {
                it.putExtra("profileName", profileName)
                it.putExtra("type", "profile")
            }
            startActivity(it)
        }
    }

    fun finish(v: View?) {
        finish()
    }
}
