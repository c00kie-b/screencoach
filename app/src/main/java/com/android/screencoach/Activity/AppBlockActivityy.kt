package com.android.achievix.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.achievix.Adapter.AppBlockAdapterr
import com.android.achievix.Model.AppBlockModell
import com.android.achievix.R
import com.android.achievix.Utility.UsageUtill.Companion.getInstalledAppsBlock

@Suppress("DEPRECATION")
class AppBlockActivityy : AppCompatActivity() {
    private var appBlockModelList: List<AppBlockModell> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var sortSpinner: Spinner
    private lateinit var searchEditText: EditText
    private var appBlockAdapterr: AppBlockAdapterr? = null
    private lateinit var appBlockLayout: LinearLayout
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var loadingLayout: LinearLayout
    private lateinit var saveButton: Button
    private var sortValue = "Name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_blockk)

        initializeViews()
        setupRecyclerView()
        setupSearchView()


        GetInstalledAppsTask(sortValue).execute()

        saveButton.setOnClickListener {
            val selectedApps = appBlockAdapterr?.getSelectedApps()
            if (selectedApps != null) {
                blockSelectedApps(selectedApps)
            }
        }
    }

    private fun initializeViews() {
        appBlockLayout = findViewById(R.id.layout_block_apps)
        loadingLayout = findViewById(R.id.loading_block_apps)
        recyclerView = findViewById(R.id.app_block_recycler_view)
        searchEditText = findViewById(R.id.search_app_block)
        saveButton = findViewById(R.id.save_button)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun setupSearchView() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun filter(text: String) {
        val filteredList = appBlockModelList.filter { it.appName.contains(text, ignoreCase = true) }
        appBlockAdapterr?.updateListBlock(filteredList)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetInstalledAppsTask(private val sort: String) : AsyncTask<Void?, Void?, List<AppBlockModell>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            appBlockLayout.visibility = View.GONE
            loadingLayout.visibility = View.VISIBLE
            saveButton.visibility = View.GONE
        }

        override fun doInBackground(vararg params: Void?): List<AppBlockModell> {
            return getInstalledAppsBlock(this@AppBlockActivityy, sort, "AppBlockActivityy")
        }

        override fun onPostExecute(result: List<AppBlockModell>?) {
            super.onPostExecute(result)
            result?.let {
                appBlockModelList = it
                appBlockAdapterr = AppBlockAdapterr(appBlockModelList)
                recyclerView.adapter = appBlockAdapterr

                appBlockLayout.visibility = View.VISIBLE
                loadingLayout.visibility = View.GONE
                saveButton.visibility = View.VISIBLE

                appBlockAdapterr?.setOnItemClickListener(object : AppBlockAdapterr.OnItemClickListener {
                    override fun onItemClick(position: Int, isChecked: Boolean) {
                        // Handle checkbox item click if needed
                    }
                })
            }
        }
    }

    private fun blockSelectedApps(selectedApps: List<AppBlockModell>) {
        val intent = Intent(this, LocationTriggerActivityy::class.java).apply {
            putParcelableArrayListExtra("selectedApps", ArrayList(selectedApps))
        }
        startActivity(intent)
    }

    fun finish(v: View?) {
        finish()
    }
}
