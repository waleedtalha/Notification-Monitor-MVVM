package com.app.fortunapaymonitor.di

import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper
import com.app.fortunapaymonitor.data.network.RemoteDataSource
import com.app.fortunapaymonitor.data.network.provideApi
import com.app.fortunapaymonitor.data.network.provideRetrofit
import com.app.fortunapaymonitor.data.repository.AuthRepository
import com.app.fortunapaymonitor.data.repository.SettingRepository
import com.app.fortunapaymonitor.viewmodel.AuthViewModel
import com.app.fortunapaymonitor.viewmodel.SettingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val myModule = module {
    single { AuthRepository(get()) }
    single { SettingRepository(get()) }
    single { provideRetrofit() }
    single { PreferenceHelper(get()) }

    factory { provideApi(get()) }
    factory { RemoteDataSource(get()) }

    viewModel { AuthViewModel(get()) }
    viewModel { SettingViewModel(get()) }
}