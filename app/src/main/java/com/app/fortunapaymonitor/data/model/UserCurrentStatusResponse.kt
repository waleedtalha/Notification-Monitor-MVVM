package com.app.fortunapaymonitor.data.model

import com.google.gson.annotations.SerializedName

data class UserCurrentStatusResponse(
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("subscription_expire")
    var subscriptionExpire: String? = null,
    @SerializedName("subscription_active")
    var subscriptionActive: Boolean? = null,
    @SerializedName("numbers")
    var numbers: ArrayList<Numbers> = arrayListOf()
)