package com.app.fortunapaymonitor.data.model

import com.google.gson.annotations.SerializedName

data class PaymentStatusResponse(
    @SerializedName("transaction_id")
    var transactionId: String? = null,
    @SerializedName("qr_code")
    var qrCode: String? = null,
    @SerializedName("image")
    var image: String? = null,
    @SerializedName("transaction_amount")
    var transactionAmount: Int? = null,
    @SerializedName("status")
    var status: String? = null,
    @SerializedName("paid")
    var paid: Int? = null,
    @SerializedName("pix_id")
    var pixId: String? = null,
    @SerializedName("datetime")
    var datetime: String? = null
)