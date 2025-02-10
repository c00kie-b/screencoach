package com.android.achievix.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.achievix.Adapter.AppSelectAdapterr
import com.android.achievix.Model.AppSelectModell
import com.android.achievix.Utility.UsageUtill
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSelectActivityy : AppCompatActivity() {
    private lateinit var appSelectModelList: List<AppSelectModell>
    private lateinit var recyclerView: RecyclerView
    private lateinit var saveButton: Button
    private lateinit var searchEditText: EditText
    private val selectedApps = mutableListOf<String>()
    private lateinit var appSelectLayout: LinearLayout
    private lateinit var loadingLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeViews()
        attachListeners()
        getInstalledApps()
    }

    private fun initializeViews() {

    }

    private fun attachListeners() {
        saveButton.setOnClickListener {
            val intent = Intent()

            selectedApps.clear()
            appSelectModelList.forEach {
                if (it.selected) {
                    selectedApps.add(it.packageName)
                }
            }

            intent.putStringArrayListExtra("selectedApps", ArrayList(selectedApps))
            setResult(RESULT_OK, intent)
            finish()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filter(text: String) {
        val filteredList = appSelectModelList.filter { it.name.contains(text, ignoreCase = true) }
        (recyclerView.adapter as AppSelectAdapterr).updateListSelect(filteredList)
    }

    private fun getInstalledApps() {
        appSelectLayout.visibility = View.GONE
        loadingLayout.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            var apps = UsageUtill.getInstalledAppsSelect(this@AppSelectActivityy)
            apps = apps.sortedBy { it.name }
            withContext(Dispatchers.Main) {
                appSelectModelList = apps
                recyclerView.adapter = AppSelectAdapterr(appSelectModelList)
                appSelectLayout.visibility = View.VISIBLE
                loadingLayout.visibility = View.GONE
            }
        }
    }

    fun finish(v: View?) {
        finish()
    }
}
