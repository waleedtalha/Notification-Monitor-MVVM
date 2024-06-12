package com.app.fortunapaymonitor.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.colman.simplecpfvalidator.isCpf
import coil.load
import com.app.fortunapaymonitor.R
import com.app.fortunapaymonitor.databinding.BsAddWpNumberBinding
import com.app.fortunapaymonitor.databinding.BsLoginBinding
import com.app.fortunapaymonitor.databinding.BsLogoutBinding
import com.app.fortunapaymonitor.databinding.BsPaymentCancelledBinding
import com.app.fortunapaymonitor.databinding.BsPaymentCompletedBinding
import com.app.fortunapaymonitor.databinding.BsRegisterBinding
import com.app.fortunapaymonitor.databinding.BsShowQrBinding
import com.app.fortunapaymonitor.utils.helpers.CpfTextWatcher
import com.app.fortunapaymonitor.utils.helpers.PhoneNumberTextWatcher
import com.app.fortunapaymonitor.utils.helpers.PreferenceHelper
import com.app.fortunapaymonitor.data.model.GenerateQrResponse
import com.app.fortunapaymonitor.data.model.PaymentStatusResponse
import com.app.fortunapaymonitor.data.model.UserDetails
import com.app.fortunapaymonitor.services.SubscriptionRenewalReminderReceiver
import com.app.fortunapaymonitor.utils.extensions.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity() {
    private var loginCallBack: (String, UserDetails) -> Unit = { _, _ -> }
    private var logoutCallBack: (Boolean) -> Unit = {}
    private var registerCallBack: (String, UserDetails) -> Unit = { _, _ -> }
    private var qrCodeCallBack: (String) -> Unit = {}
    private var paymentCompletedCallBack: (Boolean) -> Unit = {}
    private var paymentCancelledCallBack: (Boolean) -> Unit = {}
    private var addNumberCallBack: (String) -> Unit = {}
    val prefs: PreferenceHelper by inject()

    private var registerBottomSheet: BottomSheetDialog? = null
    private var qrCodeBottomSheet: BottomSheetDialog? = null
    val closeSheetValue = MutableLiveData(false)
    private val closeSheet: LiveData<Boolean> = closeSheetValue
    var actionOnPermission: ((granted: Boolean) -> Unit)? = null
    var isAskingPermissions = false
    var showSettingAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_base)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun bsLogin(isRegistering: LiveData<Boolean>, loginCallBack: (String, UserDetails) -> Unit) {
        this.loginCallBack = loginCallBack
        val loginBottomSheet: BottomSheetDialog?
        loginBottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialog).apply {
            setCancelable(false)
        }
        val binding = BsLoginBinding.inflate(LayoutInflater.from(this))
        loginBottomSheet.setContentView(binding.root)
        isRegistering.observe(this) {
            if (it) {
                binding.progressBar.beVisible()
                binding.loginBtn.isEnabled = false
            } else {
                binding.progressBar.beGone()
                binding.loginBtn.isEnabled = true
            }
        }
        closeSheet.observe(this) {
            if (it && loginBottomSheet.isShowing) {
                loginBottomSheet.dismiss()
            }
        }
        binding.loginBtn.setOnClickListener {
            if (binding.etEmail.text!!.isEmpty() || binding.etEmail.text!!.isBlank()) {
                toast(getString(R.string.enter_email))
                return@setOnClickListener
            } else if (!isValidEmail(binding.etEmail.text.toString())) {
                toast(getString(R.string.invalid_email))
                return@setOnClickListener
            } else if (binding.etPassword.text!!.isEmpty() || binding.etPassword.text!!.isBlank()) {
                toast(getString(R.string.enter_password))
                return@setOnClickListener
            } else if (binding.etPassword.text!!.length < 6) {
                toast(getString(R.string.password_too_short))
                return@setOnClickListener
            }
            val userDetails = UserDetails(
                email = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString()
            )
            loginCallBack(LOGIN, userDetails)
        }
        binding.close.setOnClickListener {
            if (loginBottomSheet.isShowing) {
                loginBottomSheet.dismiss()
            }
        }
        binding.registerHere.setOnClickListener {
            if (loginBottomSheet.isShowing) {
                loginBottomSheet.dismiss()
            }
            loginCallBack(REGISTER_HERE, UserDetails())
        }
        loginBottomSheet.show()
    }
    private fun openWebPage(url: String) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
    fun bsRegister(
        isRegistering: LiveData<Boolean>,
        registerCallBack: (String, UserDetails) -> Unit
    ) {
        this.registerCallBack = registerCallBack
        registerBottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialog).apply {
            setCancelable(false)
        }
        val binding = BsRegisterBinding.inflate(LayoutInflater.from(this))
        registerBottomSheet?.setContentView(binding.root)
        binding.etTaxId.addTextChangedListener(CpfTextWatcher(binding.etTaxId))
        isRegistering.observe(this) {
            if (it) {
                binding.progressBar.beVisible()
                binding.registerBtn.isEnabled = false
            } else {
                binding.progressBar.beGone()
                binding.registerBtn.isEnabled = true
            }
        }
        binding.privacyAndPolicy.setOnClickListener {
            openWebPage("https://fortunapay.com.br/docs/Termos.pdf")
        }
        binding.registerBtn.setOnClickListener {
            if (binding.etName.text!!.isEmpty() || binding.etName.text!!.isBlank()) {
                toast(getString(R.string.enter_full_name))
                return@setOnClickListener
            } else if (binding.etTaxId.text!!.isEmpty() || binding.etTaxId.text!!.isBlank()) {
                toast(getString(R.string.enter_tax_id))
                return@setOnClickListener
            } else if (!binding.etTaxId.text.toString().isCpf(charactersToIgnore = listOf('.', '-'))) {
                toast(getString(R.string.invalid_cpf))
                return@setOnClickListener
            } else if (binding.etTaxId.text!!.length < 14) {
                toast(getString(R.string.incomplete_tax_id))
                return@setOnClickListener
            } else if (binding.etEmail.text!!.isEmpty() || binding.etEmail.text!!.isBlank()) {
                toast(getString(R.string.enter_email))
                return@setOnClickListener
            } else if (!isValidEmail(binding.etEmail.text.toString())) {
                toast(getString(R.string.invalid_email))
                return@setOnClickListener
            } else if (binding.etPassword.text!!.isEmpty() || binding.etPassword.text!!.isBlank()) {
                toast(getString(R.string.enter_password))
                return@setOnClickListener
            } else if (binding.etPassword.text!!.length < 6) {
                toast(getString(R.string.password_too_short))
                return@setOnClickListener
            } else if (!binding.acceptPolicy.isChecked) {
                toast(getString(R.string.accept_privacy_policy))
                return@setOnClickListener
            }
            prefs.userEmail = binding.etEmail.text.toString()
            prefs.userPassword = binding.etPassword.text.toString()
            val userDetails = UserDetails(
                binding.etName.text.toString(),
                binding.etTaxId.text.toString(),
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            )
            registerCallBack(REGISTER, userDetails)
        }
        binding.close.setOnClickListener {
            if (registerBottomSheet?.isShowing == true) {
                registerBottomSheet?.dismiss()
            }
        }
        registerBottomSheet?.show()
    }

    @SuppressLint("SetTextI18n")
    fun bsShowQrCode(
        context: Context,
        qrDetails: GenerateQrResponse,
        qrCodeCallBack: (String) -> Unit
    ) {
        this.qrCodeCallBack = qrCodeCallBack
        qrCodeBottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialog).apply {
            setCancelable(false)
        }
        val binding = BsShowQrBinding.inflate(LayoutInflater.from(this))
        qrCodeBottomSheet?.setContentView(binding.root)
        binding.qrImage.load(qrDetails.image) {
            crossfade(true)
            placeholder(R.drawable.qr_default_image)
        }
        binding.amount.text = "${getString(R.string.dollar_sign)}${qrDetails.amount}"
        binding.qrCode.text = qrDetails.qrCode.toString()
        binding.copeQrCode.setOnClickListener {
            context.copyToClipboard(getString(R.string.copy_label), qrDetails.qrCode.toString())
        }
        binding.close.setOnClickListener {
            if (qrCodeBottomSheet?.isShowing == true) {
                qrCodeBottomSheet?.dismiss()
            }
            qrCodeCallBack(CLOSED)
        }

        //dismiss register sheet before showing it
        if (registerBottomSheet?.isShowing == true) {
            registerBottomSheet?.dismiss()
        }

        qrCodeBottomSheet?.show()
    }

    @SuppressLint("SetTextI18n")
    fun bsPaymentCompleted(
        paymentStatusDetails: PaymentStatusResponse,
        paymentCompletedCallBack: (Boolean) -> Unit
    ) {
        this.paymentCompletedCallBack = paymentCompletedCallBack
        val paymentCompletedBottomSheet: BottomSheetDialog?
        paymentCompletedBottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialog).apply {
            setCancelable(false)
        }
        val binding = BsPaymentCompletedBinding.inflate(LayoutInflater.from(this))
        paymentCompletedBottomSheet.setContentView(binding.root)
        binding.transactionId.text =
            "${getString(R.string.transaction_id)}: ${paymentStatusDetails.transactionId}"
        binding.dateTime.text = paymentStatusDetails.datetime
        binding.amount.text =
            "${getString(R.string.dollar_sign)}${paymentStatusDetails.transactionAmount}"
        binding.configuration.setOnClickListener {
            if (paymentCompletedBottomSheet.isShowing) {
                paymentCompletedBottomSheet.dismiss()
            }
            paymentCompletedCallBack(true)
        }

        //dismiss qr sheet before showing it
        if (qrCodeBottomSheet?.isShowing == true) {
            qrCodeBottomSheet?.dismiss()
        }

        paymentCompletedBottomSheet.show()
    }

    @SuppressLint("SetTextI18n")
    fun bsPaymentCancelled(
        paymentCancelledCallBack: (Boolean) -> Unit
    ) {
        this.paymentCancelledCallBack = paymentCancelledCallBack
        val paymentCancelledBottomSheet: BottomSheetDialog?
        paymentCancelledBottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialog).apply {
            setCancelable(false)
        }
        val binding = BsPaymentCancelledBinding.inflate(LayoutInflater.from(this))
        paymentCancelledBottomSheet.setContentView(binding.root)
        binding.backToHome.setOnClickListener {
            if (paymentCancelledBottomSheet.isShowing) {
                paymentCancelledBottomSheet.dismiss()
            }
            paymentCancelledCallBack(true)
        }

        paymentCancelledBottomSheet.show()
    }

    fun bsAddNumber(isAdding: LiveData<Boolean>, addNumberCallBack: (String) -> Unit) {
        this.addNumberCallBack = addNumberCallBack
        val loginBottomSheet: BottomSheetDialog?
        loginBottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val binding = BsAddWpNumberBinding.inflate(LayoutInflater.from(this))
        loginBottomSheet.setContentView(binding.root)
        binding.etAddNumber.addTextChangedListener(PhoneNumberTextWatcher(binding.etAddNumber))
        isAdding.observe(this) {
            if (it) {
                binding.progressBar.beVisible()
                binding.addBtn.isEnabled = false
            } else {
                binding.progressBar.beGone()
                binding.addBtn.isEnabled = true
            }
        }
        closeSheet.observe(this) {
            if (it && loginBottomSheet.isShowing) {
                loginBottomSheet.dismiss()
            }
        }
        binding.addBtn.setOnClickListener {
            val digitCount = binding.etAddNumber.text!!.length
            if (binding.etAddNumber.text!!.isEmpty() || binding.etAddNumber.text!!.isBlank()) {
                toast(getString(R.string.enter_number))
                return@setOnClickListener
            } else if (digitCount < 10 || digitCount > 16) {
                toast(getString(R.string.invalid_number))
                return@setOnClickListener
            }
            addNumberCallBack(binding.etAddNumber.text.toString().onlyDigits())
        }
        binding.close.setOnClickListener {
            if (loginBottomSheet.isShowing) {
                loginBottomSheet.dismiss()
            }
        }
        loginBottomSheet.show()
    }

    fun bsLogout(logoutCallBack: (Boolean) -> Unit) {
        this.logoutCallBack = logoutCallBack
        val logoutBottomSheet: BottomSheetDialog?
        logoutBottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val binding = BsLogoutBinding.inflate(LayoutInflater.from(this))
        logoutBottomSheet.setContentView(binding.root)
        binding.logoutBtn.setOnClickListener {
            prefs.authToken = ""
            prefs.subscriptionActivated = false
            toast(getString(R.string.logged_out))
            logoutBottomSheet.dismiss()
            logoutCallBack(true)
        }
        binding.close.setOnClickListener {
            if (logoutBottomSheet.isShowing) {
                logoutBottomSheet.dismiss()
            }
        }
        logoutBottomSheet.show()
    }

    fun handleNotificationPermission(callback: (granted: Boolean) -> Unit) {
        if (!isTiramisuPlus()) {
            callback(true)
        } else {
            handlePermission(PERMISSION_POST_NOTIFICATIONS) { granted ->
                callback(granted)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun handlePermission(permissionId: Int, callback: (granted: Boolean) -> Unit) {
        actionOnPermission = null
        if (hasPermission(permissionId)) {
            callback(true)
        } else {
            isAskingPermissions = true
            actionOnPermission = callback
            ActivityCompat.requestPermissions(
                this, arrayOf(getPermissionString(permissionId)), GENERIC_PERMISSION_HANDLER
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isAskingPermissions = false
        if (requestCode == GENERIC_PERMISSION_HANDLER) {
            for (i in permissions.indices) {
                val per: String = permissions[i]
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    val showRationale = shouldShowRequestPermissionRationale(per)
                    if (!showRationale) {
                        val builder = AlertDialog.Builder(this@BaseActivity)
                        builder.setTitle("App Permission")
                            .setMessage(R.string.access_permission_from_settings)
                            .setPositiveButton(
                                "Open Settings"
                            ) { _, _ ->
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts(
                                    "package",
                                    packageName, null
                                )
                                intent.data = uri
                                startActivityForResult(
                                    intent,
                                    OPEN_SETTINGS
                                )
                                finish()
                            }
                        showSettingAlert = builder.setCancelable(false).create()
                        showSettingAlert?.show()
                    } else {
                        ActivityCompat.requestPermissions(
                            this, arrayOf(
                                Manifest.permission.POST_NOTIFICATIONS
                            ), 0
                        )
                    }
                } else {
                    actionOnPermission?.invoke(grantResults[0] == 0)
                }
            }
        }
    }

    fun checkAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                toast(getString(R.string.alarm_permission))
                return false
            }
        }
        return true
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleSubscriptionRenewalReminder(context: Context, subscriptionExpireDate: String) {
        val timeToNotify = subscriptionExpireDate.getNotificationTime()
        val intent = Intent(context, SubscriptionRenewalReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeToNotify,
            pendingIntent
        )
    }

    fun generateTimesList(): List<String> {
        return (0..23).map { hour ->
            val period = if (hour < 12) "" else ""
            String.format("%02d:00%s", hour, period)
        }
    }

}