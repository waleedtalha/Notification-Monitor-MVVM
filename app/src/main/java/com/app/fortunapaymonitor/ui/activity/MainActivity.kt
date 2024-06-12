package com.app.fortunapaymonitor.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.fortunapaymonitor.R
import com.app.fortunapaymonitor.databinding.ActivityMainBinding
import com.app.fortunapaymonitor.utils.extensions.toast
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.lifecycleScope
import com.app.fortunapaymonitor.utils.extensions.isNotificationServiceEnabled
import com.app.fortunapaymonitor.services.NotificationForegroundService
import com.app.fortunapaymonitor.utils.extensions.*
import com.app.fortunapaymonitor.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModel()

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    var paidStatus: Boolean = false
    var transactionId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        getObservers()
        clickListeners()
    }

    @SuppressLint("ResourceAsColor")
    private fun initViews() {
        authViewModel.waitForServerAnswer.value = false
        authViewModel.apiErrorToast.observe(this) { error ->
            if (error.startsWith(CAN_NOT_DO_IT)) {
                toast(getString(R.string.already_registered))
            } else {
                toast(error.toString())
            }
        }

        authViewModel.registerUserResponse()
        authViewModel.loginUserResponse(this@MainActivity)
        authViewModel.justLoginUserResponse(this@MainActivity)
        authViewModel.generateQrResponse(this@MainActivity)
        authViewModel.paymentStatusResponse(this@MainActivity)
        authViewModel.sendMessageResponse(this@MainActivity)
        authViewModel.currentStatusResponse(this@MainActivity)
    }

    private fun getObservers() {
        lifecycleScope.launch(Dispatchers.Main) {
            authViewModel.registerUserDataFlow.collect { data ->
                //call login api after registering the fresh user for getting token
                authViewModel.loginUser(prefs.userEmail ?: "", prefs.userPassword ?: "")
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            authViewModel.loginUserDataFlow.collect { data ->
                prefs.authToken = data.accessToken.toString()
                //call qrcode api after getting the token from login api
                authViewModel.generateQR()
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            authViewModel.justLoginUserDataFlow.collect { data ->
                prefs.authToken = data.accessToken.toString()
                //call me(current status) api to get subscription status
                authViewModel.getCurrentStatus()
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            authViewModel.currentStatusDataFlow.collect { data ->
                toast(getString(R.string.logged_in))
                prefs.subscriptionActivated = data.subscriptionActive
                prefs.subscriptionExpireDate = data.subscriptionExpire
                closeSheetValue.value = true
                binding.makePaymentBtn.beVisibleIf(prefs.subscriptionActivated == false)
                if (data.subscriptionActive == true && checkAlarmPermission()) {
                    scheduleSubscriptionRenewalReminder(
                        this@MainActivity,
                        data.subscriptionExpire ?: ""
                    )
                }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            authViewModel.generateQrDataFlow.collect { data ->
                transactionId = data.transactionId ?: ""
                bsShowQrCode(this@MainActivity, data) {
                    when (it) {
                        CLOSED -> {
                            toast(getString(R.string.make_payment_to_monitor))
                            binding.makePaymentBtn.beVisibleIf(prefs.subscriptionActivated == false)
                            handler.removeCallbacks(runnable)
                        }
                    }
                }
                //continuously call payment status api to update payment status
                if (!paidStatus) {
                    startCallingStatusApi()
                }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            authViewModel.paymentStatusDataFlow.collect { data ->
                if (data.paid?.equals(1) == true) {
//                if (paidStatus){
                    handler.removeCallbacks(runnable)
                    paidStatus = true
                    prefs.subscriptionActivated = true
                    binding.makePaymentBtn.beGone()
                    bsPaymentCompleted(data) {
                        if (it) {
                            prefs.subscriptionExpired = false
                            startActivity(Intent(this@MainActivity,SettingsActivity::class.java))
                        }
                    }
                } else if (data.status?.startsWith("CANCEL") == true) {
                    bsPaymentCancelled {
                        if (it) {
                            binding.makePaymentBtn.beVisible()
                        }
                    }
                }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            authViewModel.sendMessageDataFlow.collect { data ->
//                toast("sent")
                Log.wtf("sent", "yes")
            }
        }
    }

    private fun clickListeners() {
        binding.startMonitoringBtn.setOnClickListener {
            if (!this.isInternetAvailable()){
                toast(getString(R.string.internet_not_available))
            } else if (!checkAlarmPermission()) {
//                toast(getString(R.string.alarm_permission))
            } else if (prefs.subscriptionActivated == false) {
                toast(getString(R.string.subscription_not_active))
                if (prefs.authToken?.isEmpty() == true) {
                    loginSheet()
                }
            } else if (prefs.enabledAppList.isEmpty()) {
                toast(getString(R.string.select_one_app_settings))
            } else if (prefs.isOneNumberAdded == false) {
                toast(getString(R.string.add_one_number))
            } else if (!isNotificationServiceEnabled(this)) {
                handleNotificationPermission { granted ->
                    if (granted) {
                        //start monitoring here
                        toast(getString(R.string.enable_permission))
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        startActivity(intent)
                    }
                }
            } else {
                //stop monitoring here
                toast(getString(R.string.disable_permission))
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
        }
        binding.makePaymentBtn.setOnClickListener {
            authViewModel.generateQR()
            authViewModel.waitForServer.observe(this) {
                binding.progressBar.beVisibleIf(it)
                binding.makePaymentBtn.isEnabled = !it
            }
        }
        binding.authentication.setOnClickListener {
            if (prefs.authToken?.isEmpty() == true) {
                //login user
                loginSheet()
            } else {
                //logout user
                if (isNotificationServiceEnabled(this)) {
                    toast(getString(R.string.stop_monitoring_to_logout))
                } else {
                    bsLogout {
                        if (it) {
                            binding.makePaymentBtn.beGone()
                        }
                    }
                }
            }

        }
        binding.settings.setOnClickListener {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
        }
    }

    private fun loginSheet() {
        bsLogin(authViewModel.waitForServer) { loginCallBack, loginUserDetails ->
            when (loginCallBack) {
                LOGIN -> {
                    authViewModel.justLoginUser(
                        loginUserDetails.email ?: "",
                        loginUserDetails.password ?: ""
                    )
                }

                REGISTER_HERE -> {
                    bsRegister(authViewModel.waitForServer) { registerCallback, userDetails ->
                        when (registerCallback) {
                            REGISTER -> {
                                authViewModel.registerUser(userDetails)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startCallingStatusApi() {
        runnable = object : Runnable {
            override fun run() {

                Log.wtf("calling status api", "yes")
                authViewModel.getPaymentStatus(transactionId)

                // Stop calling the function if the desired response is met
                if (paidStatus) {
                    handler.removeCallbacks(this)
                } else {
                    handler.postDelayed(this, 5000)
                }
            }
        }
        handler.postDelayed(runnable, 5000)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("ResourceAsColor")
    override fun onResume() {
        super.onResume()
        if (isNotificationServiceEnabled(this)) {
            //monitoring started UI
            binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.light_yellow))
            binding.welcomeText.text = getString(R.string.monitoring_your_apps)
            binding.monitoringAppsDesc.text = getString(R.string.monitoring_apps_click_end_to_stop)
            binding.startMonitoringBtn.apply {
                text = getString(R.string.stop_monitoring)
                setBackgroundResource(R.drawable.stop_monitoring_btn_bg)
                setTextColor(R.color.red)
            }
            prefs.receiverRegistered = true

            //show renew subscription button
            if (prefs.subscriptionExpired == true) {
                binding.makePaymentBtn.beVisible()
            } else {
                binding.makePaymentBtn.beGone()
            }
        } else {
            binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
            binding.welcomeText.text = getString(R.string.welcome_to_monitor_notification)
            binding.monitoringAppsDesc.text =
                getString(R.string.click_start_to_monitoring_your_app_s_notifications)
            binding.startMonitoringBtn.apply {
                text = getString(R.string.start_monitoring)
                setBackgroundResource(R.drawable.btn_bg)
                setTextColor(R.color.black)
            }
            if (prefs.receiverRegistered == true) {
                Intent(this, NotificationForegroundService::class.java).also { intent ->
                    stopService(intent)
                }
                prefs.receiverRegistered = false
            }
        }
    }
}