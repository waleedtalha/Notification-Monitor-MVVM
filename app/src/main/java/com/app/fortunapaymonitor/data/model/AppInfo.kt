package com.app.fortunapaymonitor.data.model

import android.graphics.drawable.Drawable


data class AppInfo(
    val appName: String? = null,
    val packageName: String? = null,
    val appIcon: Drawable? = null,
    var isSwitchEnabled: Boolean?=false
)
