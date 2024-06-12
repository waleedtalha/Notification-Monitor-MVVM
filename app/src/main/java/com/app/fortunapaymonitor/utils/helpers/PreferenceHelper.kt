package com.app.fortunapaymonitor.utils.helpers

import android.content.Context
import android.content.SharedPreferences
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.AUTH_TOKEN
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.ENABLED_APPS
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.FROM_TIME
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.MONITORING_TIME
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.NUMBER_ADDED
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.RECEIVER_REGISTERED
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.SELECTED_DAYS
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.SUBSCRIPTION
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.SUBSCRIPTION_EXPIRE
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.SUBSCRIPTION_EXPIRE_DATE
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.TO_TIME
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.USER_EMAIL
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper.PreferenceVariable.USER_PASS

class PreferenceHelper(context: Context) {
    private val appPrefs: SharedPreferences =
        context.getSharedPreferences("monitor_pref", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = appPrefs.edit()

    object PreferenceVariable {
        const val USER_EMAIL = "user_email"
        const val USER_PASS = "user_pass"
        const val AUTH_TOKEN = "auth_token"
        const val ENABLED_APPS = "enabled_apps"
        const val SELECTED_DAYS = "selected_days"
        const val SUBSCRIPTION = "subscription"
        const val RECEIVER_REGISTERED = "registered"
        const val NUMBER_ADDED = "number_added"
        const val SUBSCRIPTION_EXPIRE = "subs_expire"
        const val SUBSCRIPTION_EXPIRE_DATE = "subs_expire_date"
        const val FROM_TIME = "from_time"
        const val TO_TIME = "to_time"
        const val MONITORING_TIME = "monitor_time"
    }

    init {
        editor.apply()
    }

    var userEmail: String?
        get() = appPrefs.getString(USER_EMAIL, "")
        set(email) {
            editor.putString(USER_EMAIL, email)
            editor.apply()
        }

    var userPassword: String?
        get() = appPrefs.getString(USER_PASS, "")
        set(pass) {
            editor.putString(USER_PASS, pass)
            editor.apply()
        }

    var authToken: String?
        get() = appPrefs.getString(AUTH_TOKEN, "")
        set(token) {
            editor.putString(AUTH_TOKEN, token)
            editor.apply()
        }
    var subscriptionExpireDate: String?
        get() = appPrefs.getString(SUBSCRIPTION_EXPIRE_DATE, "")
        set(date) {
            editor.putString(SUBSCRIPTION_EXPIRE_DATE, date)
            editor.apply()
        }

    var subscriptionActivated: Boolean?
        get() = appPrefs.getBoolean(SUBSCRIPTION, false)
        set(token) {
            if (token != null) {
                editor.putBoolean(SUBSCRIPTION, token)
            }
            editor.apply()
        }
    var subscriptionExpired: Boolean?
        get() = appPrefs.getBoolean(SUBSCRIPTION_EXPIRE, false)
        set(expire) {
            if (expire != null) {
                editor.putBoolean(SUBSCRIPTION_EXPIRE, expire)
            }
            editor.apply()
        }
    var isOneNumberAdded: Boolean?
        get() = appPrefs.getBoolean(NUMBER_ADDED, false)
        set(numberAdded) {
            if (numberAdded != null) {
                editor.putBoolean(NUMBER_ADDED, numberAdded)
            }
            editor.apply()
        }

    var receiverRegistered: Boolean?
        get() = appPrefs.getBoolean(RECEIVER_REGISTERED, false)
        set(token) {
            if (token != null) {
                editor.putBoolean(RECEIVER_REGISTERED, token)
            }
            editor.apply()
        }
    var monitoringTime24: Boolean?
        get() = appPrefs.getBoolean(MONITORING_TIME, true)
        set(monitor) {
            if (monitor != null) {
                editor.putBoolean(MONITORING_TIME, monitor)
            }
            editor.apply()
        }

    var enabledAppList: Set<String>
        get() = appPrefs.getStringSet(ENABLED_APPS, emptySet()) ?: emptySet()
        set(apps) {
            editor.putStringSet(ENABLED_APPS, apps)
            editor.apply()
        }

    var selectedDaysList: Set<String>
        get() = appPrefs.getStringSet(SELECTED_DAYS, emptySet()) ?: emptySet()
        set(days) {
            editor.putStringSet(SELECTED_DAYS, days)
            editor.apply()
        }
    var fromTime: String?
        get() = appPrefs.getString(FROM_TIME, "00:00")
        set(from) {
            editor.putString(FROM_TIME, from)
            editor.apply()
        }
    var toTime: String?
        get() = appPrefs.getString(TO_TIME, "23:00")
        set(to) {
            editor.putString(TO_TIME, to)
            editor.apply()
        }
    fun clearPreference() {
        editor.clear()
        editor.apply()
    }
}