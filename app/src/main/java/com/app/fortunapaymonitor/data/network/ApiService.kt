package com.app.fortunapaymonitor.data.network

import com.app.fortunapaymonitor.data.model.GenerateQrResponse
import com.app.fortunapaymonitor.data.model.LoginResponse
import com.app.fortunapaymonitor.data.model.PaymentStatusResponse
import com.app.fortunapaymonitor.data.model.RegisterResponse
import com.app.fortunapaymonitor.data.model.SendMessageResponse
import com.app.fortunapaymonitor.data.model.UserCurrentStatusResponse
import com.app.fortunapaymonitor.utils.extensions.ADD_NUMBER_EP
import com.app.fortunapaymonitor.utils.extensions.CANCEL_NUMBER_EP
import com.app.fortunapaymonitor.utils.extensions.CURRENT_STATUS_EP
import com.app.fortunapaymonitor.utils.extensions.GENERATE_QR_EP
import com.app.fortunapaymonitor.utils.extensions.LOGIN_EP
import com.app.fortunapaymonitor.utils.extensions.PAYMENT_STATUS_EP
import com.app.fortunapaymonitor.utils.extensions.REGISTER_EP
import com.app.fortunapaymonitor.utils.extensions.SEND_MESSAGE_EP
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST(REGISTER_EP)
    suspend fun registerUser(@Body request: RequestBody): Response<RegisterResponse>

    @POST(LOGIN_EP)
    suspend fun loginUser(@Body request: RequestBody): Response<LoginResponse>

    @POST(GENERATE_QR_EP)
    suspend fun generateQR(): Response<GenerateQrResponse>

    @GET("$PAYMENT_STATUS_EP{transactionId}")
    suspend fun getPaymentStatus(@Path("transactionId") transactionId: String): Response<PaymentStatusResponse>

    @GET(CURRENT_STATUS_EP)
    suspend fun getCurrentStatus(): Response<UserCurrentStatusResponse>

    @POST(SEND_MESSAGE_EP)
    suspend fun sendMessageToWP(@Body request: RequestBody): Response<SendMessageResponse>

    @POST(ADD_NUMBER_EP)
    suspend fun addNumber(@Body request: RequestBody): Response<SendMessageResponse>

    @POST(CANCEL_NUMBER_EP)
    suspend fun cancelNumber(@Body request: RequestBody): Response<SendMessageResponse>
}