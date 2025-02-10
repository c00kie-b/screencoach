package com.android.achievix.Model

import android.graphics.drawable.Drawable

data class AppBlockModel(
    val appName: String,
    val icon: Drawable,
    val usageOrNetwork: String,
    val extra: String,
    val blocked: Boolean,
    val packageName: String,
    var isChecked: Boolean // Add this line to include the checkbox state
)
