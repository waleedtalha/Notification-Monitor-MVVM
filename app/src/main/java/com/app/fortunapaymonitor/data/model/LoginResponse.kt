package com.app.fortunapaymonitor.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token")
    var accessToken: String? = null,
    @SerializedName("token_type")
    var tokenType: String? = null,
    @SerializedName("expires_in")
    var expiresIn: Int? = null
)
