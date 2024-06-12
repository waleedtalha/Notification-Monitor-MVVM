package com.app.fortunapaymonitor.utils.extensions

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun String.onlyDigits(): String = this.filter { it.isDigit() }

fun String.formatAsPhoneNumber(): String {
    // Ensure the string consists only of digits
    val digits = this.filter { it.isDigit() }

    // Build the formatted phone number according to the pattern
    return buildString {
        if (digits.length >= 2) {
            append("(${digits.substring(0, 2)}) ")
        }
        if (digits.length >= 3) {
            append("${digits[2]} ")
        }
        if (digits.length > 3) {
            append(digits.substring(3, digits.length.coerceAtMost(7)))
            if (digits.length > 7) {
                append("-${digits.substring(7)}")
            }
        }
    }
}

fun String.getNotificationTime(): Long {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val expirationDate = format.parse(this) ?: return 0L
    val calendar = Calendar.getInstance()
    calendar.time = expirationDate
    calendar.add(Calendar.DAY_OF_YEAR, -3)
    return calendar.timeInMillis
}

fun getTestNotificationTime(): Long {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.SECOND, 1)
    return calendar.timeInMillis
}