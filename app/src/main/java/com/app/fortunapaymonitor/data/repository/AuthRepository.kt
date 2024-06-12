package com.app.fortunapaymonitor.data.repository

import com.app.fortunapaymonitor.data.model.GenerateQrResponse
import com.app.fortunapaymonitor.data.model.LoginResponse
import com.app.fortunapaymonitor.data.model.PaymentStatusResponse
import com.app.fortunapaymonitor.data.model.RegisterResponse
import com.app.fortunapaymonitor.data.model.SendMessageResponse
import com.app.fortunapaymonitor.data.model.UserCurrentStatusResponse
import com.app.fortunapaymonitor.data.network.RemoteDataSource
import com.app.fortunapaymonitor.data.network.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AuthRepository(private val remoteDataSource: RemoteDataSource) {
    val registerUserFlow = MutableSharedFlow<Resource<RegisterResponse>>(replay = 0)
    val loginUserFlow = MutableSharedFlow<Resource<LoginResponse>>(replay = 0)
    val justLoginUserFlow = MutableSharedFlow<Resource<LoginResponse>>(replay = 0)
    val generateQrUserFlow = MutableSharedFlow<Resource<GenerateQrResponse>>(replay = 0)
    val paymentStatusUserFlow = MutableSharedFlow<Resource<PaymentStatusResponse>>(replay = 0)
    val sendMessageUserFlow = MutableSharedFlow<Resource<SendMessageResponse>>(replay = 0)
    val currentStatusFlow = MutableSharedFlow<Resource<UserCurrentStatusResponse>>(replay = 0)

    suspend fun registerUser(name: String, email: String, password: String, taxId: String) {
        val gson = Gson()
        val requestBodyJson = gson.toJson(
            mapOf(
                "name" to name,
                "email" to email,
                "password" to password,
                "tax_id" to taxId
            )
        )
        val requestBody = requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull())
        registerUserFlow.emit(Resource.loading(null))
        val response = remoteDataSource.registerUser(requestBody)
        registerUserFlow.emit(response)
    }
    suspend fun loginUser(email: String, password: String) {
        val gson = Gson()
        val requestBodyJson = gson.toJson(
            mapOf(
                "email" to email,
                "password" to password
            )
        )
        val requestBody = requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull())
        loginUserFlow.emit(Resource.loading(null))
        val response = remoteDataSource.loginUser(requestBody)
        loginUserFlow.emit(response)
    }
    suspend fun justLoginUser(email: String, password: String) {
        val gson = Gson()
        val requestBodyJson = gson.toJson(
            mapOf(
                "email" to email,
                "password" to password
            )
        )
        val requestBody = requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull())
        justLoginUserFlow.emit(Resource.loading(null))
        val response = remoteDataSource.loginUser(requestBody)
        justLoginUserFlow.emit(response)
    }
    suspend fun sendMessageToWP(message: String) {
        val gson = Gson()
        val requestBodyJson = gson.toJson(
            mapOf(
                "message" to message
            )
        )
        val requestBody = requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull())
        sendMessageUserFlow.emit(Resource.loading(null))
        val response = remoteDataSource.sendMessageToWP(requestBody)
        sendMessageUserFlow.emit(response)
    }
    suspend fun generateQR() {
        generateQrUserFlow.emit(Resource.loading(null))
        val response = remoteDataSource.generateQR()
        generateQrUserFlow.emit(response)
    }
    suspend fun getPaymentStatus(transactionId: String) {
        paymentStatusUserFlow.emit(Resource.loading(null))
        val response = remoteDataSource.getPaymentStatus(transactionId)
        paymentStatusUserFlow.emit(response)
    }
    suspend fun getCurrentStatus() {
        currentStatusFlow.emit(Resource.loading(null))
        val response = remoteDataSource.getCurrentStatus()
        currentStatusFlow.emit(response)
    }
}