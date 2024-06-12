package com.app.fortunapaymonitor.viewmodel

import android.content.Context
import android.util.Log
import com.app.fortunapaymonitor.R
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.fortunapaymonitor.data.model.GenerateQrResponse
import com.app.fortunapaymonitor.data.model.LoginResponse
import com.app.fortunapaymonitor.data.model.PaymentStatusResponse
import com.app.fortunapaymonitor.data.model.RegisterResponse
import com.app.fortunapaymonitor.data.model.SendMessageResponse
import com.app.fortunapaymonitor.data.model.UserCurrentStatusResponse
import com.app.fortunapaymonitor.data.model.UserDetails
import com.app.fortunapaymonitor.data.network.Status
import com.app.fortunapaymonitor.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    val registerUserDataFlow = MutableSharedFlow<RegisterResponse>(replay = 0)
    val loginUserDataFlow = MutableSharedFlow<LoginResponse>(replay = 0)
    val justLoginUserDataFlow = MutableSharedFlow<LoginResponse>(replay = 0)
    val generateQrDataFlow = MutableSharedFlow<GenerateQrResponse>(replay = 0)
    val paymentStatusDataFlow = MutableSharedFlow<PaymentStatusResponse>(replay = 0)
    val sendMessageDataFlow = MutableSharedFlow<SendMessageResponse>(replay = 0)
    val currentStatusDataFlow = MutableSharedFlow<UserCurrentStatusResponse>(replay = 0)

    private val showApiError = MutableLiveData<String>()
    val apiErrorToast: LiveData<String> = showApiError

    val waitForServerAnswer = MutableLiveData<Boolean>()
    val waitForServer: LiveData<Boolean> = waitForServerAnswer

    fun registerUser(userDetails: UserDetails) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.registerUser(
                userDetails.name ?: "",
                userDetails.email ?: "",
                userDetails.password ?: "",
                userDetails.taxId ?: ""
            )
        }
    }

    fun justLoginUser(email: String, pass: String) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.justLoginUser(email, pass)
        }
    }

    fun loginUser(email: String, pass: String) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.loginUser(email, pass)
        }
    }

    fun generateQR() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.generateQR()
        }
    }

    fun getPaymentStatus(transactionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.getPaymentStatus(transactionId)
        }
    }

    fun sendMessageToWP(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.sendMessageToWP(message)
        }
    }

    fun getCurrentStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.getCurrentStatus()
        }
    }

    fun registerUserResponse() {
        viewModelScope.launch(Dispatchers.Main) {
            authRepository.registerUserFlow.collect { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.data?.code?.equals(1) == true) {
                            resource.data.let {
                                registerUserDataFlow.emit(it)
//                                waitForServerAnswer.value = false
                            }
                        } else {
                            showApiError.value = resource.data?.message.toString()
                            waitForServerAnswer.value = false
                        }
                    }

                    Status.LOADING -> {
                        waitForServerAnswer.value = true
                    }

                    Status.ERROR -> {
                        if (resource.data == null) {
                            showApiError.value = resource.message.toString()
                        }
                        waitForServerAnswer.value = false
                    }
                }
            }
        }
    }

    fun loginUserResponse(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            authRepository.loginUserFlow.collect { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.data != null) {
                            resource.data.let {
                                loginUserDataFlow.emit(it)
//                                waitForServerAnswer.value = false
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
                        if (resource.code?.equals(401) == true) {
                            showApiError.value = context.getString(R.string.unauthorized_user)
                        }
                        waitForServerAnswer.value = false
                    }
                }
            }
        }
    }

    fun justLoginUserResponse(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            authRepository.justLoginUserFlow.collect { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.data != null) {
                            resource.data.let {
                                justLoginUserDataFlow.emit(it)
//                                waitForServerAnswer.value = false
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
                        if (resource.code?.equals(401) == true) {
                            showApiError.value = context.getString(R.string.incorrect_email_pass)
                        } else if (resource.data == null) {
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
            authRepository.currentStatusFlow.collect { resource ->
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

    fun generateQrResponse(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            authRepository.generateQrUserFlow.collect { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.data != null) {
                            resource.data.let {
                                generateQrDataFlow.emit(it)
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
                        if (resource.data == null) {
                            showApiError.value = resource.message.toString()
                        }
                        waitForServerAnswer.value = false
                    }
                }
            }
        }
    }

    fun paymentStatusResponse(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            authRepository.paymentStatusUserFlow.collect { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.data != null) {
                            resource.data.let {
                                paymentStatusDataFlow.emit(it)
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
                        if (resource.data == null) {
                            showApiError.value = resource.message.toString()
                        }
                        waitForServerAnswer.value = false
                    }
                }
            }
        }
    }

    fun sendMessageResponse(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            authRepository.sendMessageUserFlow.collect { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (resource.data != null) {
                            resource.data.let {
                                sendMessageDataFlow.emit(it)
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
}