package com.android.achievix.Model

import android.graphics.drawable.Drawable

data class AppUsageModell(
    val name: String,
    var packageName: String?,
    val icon: Drawable,
    var extra: String?,
    var progress: Double?
)