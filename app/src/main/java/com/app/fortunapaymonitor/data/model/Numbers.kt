package com.app.fortunapaymonitor.data.model

import com.google.gson.annotations.SerializedName

data class Numbers(
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("number")
    var number: String? = null,
    @SerializedName("enable")
    var enable: Int? = null,
    @SerializedName("user_id")
    var userId: Int? = null,
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("updated_at")
    var updatedAt: String? = null
)