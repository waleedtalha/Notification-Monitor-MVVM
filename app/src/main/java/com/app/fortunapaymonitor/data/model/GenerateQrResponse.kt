package com.app.fortunapaymonitor.data.model

import com.google.gson.annotations.SerializedName

data class GenerateQrResponse(
    @SerializedName("transaction_id")
    var transactionId: String? = null,
    @SerializedName("qr_code")
    var qrCode: String? = null,
    @SerializedName("image")
    var image: String? = null,
    @SerializedName("amount")
    var amount: String? = null,
    @SerializedName("status")
    var status: String? = null
)