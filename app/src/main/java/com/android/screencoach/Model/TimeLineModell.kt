package com.android.achievix.Model

import com.android.achievix.Utility.ItemStatuss

data class TimeLineModell(
    val heading: String,
    var text: String,
    var status: ItemStatuss
)