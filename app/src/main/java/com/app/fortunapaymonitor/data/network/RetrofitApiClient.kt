package com.app.fortunapaymonitor.data.network

import com.app.fortunapaymonitor.utils.extensions.BASE_URL
import com.app.fortunapaymonitor.utils.helpers.AuthInterceptor
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

fun provideRetrofit(): Retrofit {
    val okHttpClient = OkHttpClient().newBuilder()
        .callTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .addInterceptor(AuthInterceptor())
        .build()

    return Retrofit.Builder().baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()
}
fun provideApi(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)