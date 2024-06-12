package com.app.fortunapaymonitor.data.repository

import com.app.fortunapaymonitor.data.model.SendMessageResponse
import com.app.fortunapaymonitor.data.model.UserCurrentStatusResponse
import com.app.fortunapaymonitor.data.network.RemoteDataSource
import com.app.fortunapaymonitor.data.network.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class SettingRepository(private val remoteDataSource: RemoteDataSource) {
    val addNumberFlow = MutableSharedFlow<Resource<SendMessageResponse>>(replay = 0)
    val cancelNumberFlow = MutableSharedFlow<Resource<SendMessageResponse>>(replay = 0)
    val currentStatusFlow = MutableSharedFlow<Resource<UserCurrentStatusResponse>>(replay = 0)


    suspend fun addNumber(number: String) {
        val gson = Gson()
        val requestBodyJson = gson.toJson(
            mapOf(
                "number" to number
            )
        )
        val requestBody = requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull())
        addNumberFlow.emit(Resource.loading(null))
        val response = remoteDataSource.addNumber(requestBody)
        addNumberFlow.emit(response)
    }
    suspend fun cancelNumber(number: String) {
        val gson = Gson()
        val requestBodyJson = gson.toJson(
            mapOf(
                "number" to number
            )
        )
        val requestBody = requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull())
        cancelNumberFlow.emit(Resource.loading(null))
        val response = remoteDataSource.cancelNumber(requestBody)
        cancelNumberFlow.emit(response)
    }
    suspend fun getCurrentStatus() {
        currentStatusFlow.emit(Resource.loading(null))
        val response = remoteDataSource.getCurrentStatus()
        currentStatusFlow.emit(response)
    }

}