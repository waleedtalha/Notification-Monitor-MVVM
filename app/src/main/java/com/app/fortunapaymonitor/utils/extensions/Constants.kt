package com.app.fortunapaymonitor.utils.extensions

import android.os.Build
import android.os.Looper
import androidx.annotation.ChecksSdkIntAtLeast

const val BASE_URL = "https://ws.fortunapay.com.br/"
const val REGISTER_EP = "register"
const val LOGIN_EP = "login"
const val GENERATE_QR_EP = "generate"
const val PAYMENT_STATUS_EP = "status/"
const val CURRENT_STATUS_EP = "me"
const val SEND_MESSAGE_EP = "ping"
const val ADD_NUMBER_EP = "set"
const val CANCEL_NUMBER_EP = "cancel"
const val REGISTER_HERE = "register_here"
const val LOGIN = "login"
const val REGISTER = "register"
const val CLOSED = "close"
const val NUMBER_ADDED = "number_added"
const val CAN_NOT_DO_IT = "You can"
const val PLAN_EXPIRED = "Your plan has expired"
const val NUMBER_REGISTERED = "Number registred"
const val NUMBER_CANCELLED = "Number cancelled"
const val NOTIFICATION_TITLE = "NOTIFICATION TITLE"
const val NOTIFICATION_TEXT = "NOTIFICATION TEXT"
const val GET_NOTIFICATION_DATA = "GET_NOTIFICATION_DATA"
const val PERMISSION_POST_NOTIFICATIONS = 5
const val GENERIC_PERMISSION_HANDLER = 1
const val OPEN_SETTINGS = 6

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

//check if device is running on Android 12 or higher
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun isTiramisuPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU