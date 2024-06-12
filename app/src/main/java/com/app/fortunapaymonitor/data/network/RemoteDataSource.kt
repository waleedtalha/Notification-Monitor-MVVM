package com.app.fortunapaymonitor.data.network

import com.app.fortunapaymonitor.data.model.SendMessageResponse
import okhttp3.RequestBody

class RemoteDataSource(
    private val apiService: ApiService
) : BaseDataSource() {

    suspend fun registerUser(
        request: RequestBody
    ) = getResult {
        apiService.registerUser(
            request = request
        )
    }

    suspend fun loginUser(
        request: RequestBody
    ) = getResult {
        apiService.loginUser(
            request = request
        )
    }

    suspend fun addNumber(
        request: RequestBody
    ) = getResult {
        apiService.addNumber(
            request = request
        )
    }
    suspend fun cancelNumber(
        request: RequestBody
    ) = getResult {
        apiService.cancelNumber(
            request = request
        )
    }

    suspend fun sendMessageToWP(request: RequestBody): Resource<SendMessageResponse> {
        return getResult {
            apiService.sendMessageToWP(request)
        }
    }

    suspend fun generateQR() = getResult { apiService.generateQR() }

    suspend fun getCurrentStatus() = getResult { apiService.getCurrentStatus() }

    suspend fun getPaymentStatus(transactionId: String) =
        getResult { apiService.getPaymentStatus(transactionId) }
}