package com.app.fortunapaymonitor.ui.clicklistener

import com.app.fortunapaymonitor.data.model.AppInfo

interface EnableAppListener {
    fun enableAppListener(isEnable:Boolean,position:Int,appInfo: AppInfo)
}