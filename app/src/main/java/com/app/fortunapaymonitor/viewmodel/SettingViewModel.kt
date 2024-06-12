package com.app.fortunapaymonitor.viewmodel

import androidx.lifecycle.ViewModel
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.app.fortunapaymonitor.R
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.fortunapaymonitor.data.model.AppInfo
import com.app.fortunapaymonitor.data.model.SendMessageResponse
import com.app.fortunapaymonitor.data.model.UserCurrentStatusResponse
import com.app.fortunapaymonitor.data.network.Status
import com.app.fortunapaymonitor.data.repository.SettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingViewModel(private val settingRepository: SettingRepository) : ViewModel() {

    val addNumberDataFlow = MutableSharedFlow<SendMessageResponse>(replay = 0)
    val cancelNumberDataFlow = MutableSharedFlow<SendMessageResponse>(replay = 0)
    val currentStatusDataFlow = MutableSharedFlow<UserCurrentStatusResponse>(replay = 1)

    private val showApiError = MutableLiveData<String>()
    val apiErrorToast: LiveData<String> = showApiError

    val waitForServerAnswer = MutableLiveData<Boolean>()
    val waitForServer: LiveData<Boolean> = waitForServerAnswer

    fun addNumber(number: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.addNumber(number)
        }
    }
    fun cancelNumber(number: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.cancelNumber(number)
        }
    }
    fun getCurrentStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            settingRepository.getCurrentStatus()
        }
    }

    fun addNumberResponse(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            settingRepository.addNumberFlow.collect { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.data != null) {
                            resource.data.let {
                                addNumberDataFlow.emit(it)
                                waitForServerAnswer.value = false
                            }
                        } else {
                            showApiError.value = context.getString(R.string.something_went_wrong)
                            waitForServerAnswer.value = false
                        }
                    }

                    Status.LOADING -> {
                        waitForServerAnswer.value = true
                    }

                    Status.ERROR -> {
                        if (resource.code?.equals(200) != true) {
                            showApiError.value = resource.message.toString()
                        }
                        waitForServerAnswer.value = false
                    }
                }
            }
        }
    }
    fun cancelNumberResponse(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            settingRepository.cancelNumberFlow.collect { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.data != null) {
                            resource.data.let {
                                cancelNumberDataFlow.emit(it)
                                waitForServerAnswer.value = false
                            }
                        } else {
                            showApiError.value = context.getString(R.string.something_went_wrong)
                            waitForServerAnswer.value = false
                        }
                    }

                    Status.LOADING -> {
                        waitForServerAnswer.value = true
                    }

                    Status.ERROR -> {
                        if (resource.code?.equals(200) != true) {
                            showApiError.value = resource.message.toString()
                        }
                        waitForServerAnswer.value = false
                    }
                }
            }
        }
    }

    fun currentStatusResponse(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            settingRepository.currentStatusFlow.collect { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.data != null) {
                            resource.data.let {
                                currentStatusDataFlow.emit(it)
                                waitForServerAnswer.value = false
                            }
                        } else {
                            showApiError.value = context.getString(R.string.something_went_wrong)
                            waitForServerAnswer.value = false
                        }
                    }

                    Status.LOADING -> {
                        waitForServerAnswer.value = true
                    }

                    Status.ERROR -> {
                        if (resource.code?.equals(200) != true) {
//                            showApiError.value = context.getString(R.string.something_went_wrong)
                        }
                        waitForServerAnswer.value = false
                    }
                }
            }
        }
    }
    suspend fun getDownloadedApps(packageManager: PackageManager): List<AppInfo> {
        return withContext(Dispatchers.IO){
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val downloadedApps = mutableListOf<AppInfo>()

            for (app in apps) {
                // Check if the app is not a system app
                if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                    val appName = app.loadLabel(packageManager).toString()
                    val packageName = app.packageName
                    val appIcon = app.loadIcon(packageManager)
                    downloadedApps.add(AppInfo(appName, packageName, appIcon))
                }
            }
            downloadedApps
        }
    }
}