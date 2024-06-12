package com.app.fortunapaymonitor.data.model

import com.google.gson.annotations.SerializedName

data class SendMessageResponse(
    @SerializedName("code")
    var code: Int? = null,
    @SerializedName("message")
    var message: String? = null
)