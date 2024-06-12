package com.app.fortunapaymonitor.ui.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.fortunapaymonitor.R
import com.app.fortunapaymonitor.databinding.ActivitySettingsBinding
import com.app.fortunapaymonitor.utils.extensions.*
import com.app.fortunapaymonitor.data.model.AppInfo
import com.app.fortunapaymonitor.data.model.Day
import com.app.fortunapaymonitor.data.model.Numbers
import com.app.fortunapaymonitor.ui.adapter.AppsAdapter
import com.app.fortunapaymonitor.ui.adapter.DaysAdapter
import com.app.fortunapaymonitor.ui.adapter.ShowNumbersAdapter
import com.app.fortunapaymonitor.ui.clicklistener.DaySelectionListener
import com.app.fortunapaymonitor.ui.clicklistener.EnableAppListener
import com.app.fortunapaymonitor.ui.clicklistener.RemoveNumberListener
import com.app.fortunapaymonitor.viewmodel.SettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class SettingsActivity : BaseActivity(), EnableAppListener, RemoveNumberListener,
    DaySelectionListener {
    private lateinit var binding: ActivitySettingsBinding
    private val settingViewModel: SettingViewModel by viewModel()
    private lateinit var daysOfWeek: List<Day>
    private lateinit var daysAdapter: DaysAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        getAllApps()
        getObservers()
        clickListeners()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initViews() {

        binding.activeTimeGroup.check(checkedRadioButton())
        binding.fromTime.text = prefs.fromTime
        binding.toTime.text = prefs.toTime
        settingViewModel.waitForServerAnswer.value = false
        settingViewModel.apiErrorToast.observe(this) { error ->
            toast(error.toString())
        }

        binding.back.setOnClickListener { finish() }
        binding.saveSettingBtn.setOnClickListener {
            if (prefs.enabledAppList.isEmpty()) {
                toast(getString(R.string.select_one_app))
            } else {
                finish()
            }
        }
        settingViewModel.addNumberResponse(this@SettingsActivity)
        settingViewModel.currentStatusResponse(this@SettingsActivity)
        settingViewModel.cancelNumberResponse(this@SettingsActivity)

        //call me api to get and show whatsap numbers
        settingViewModel.getCurrentStatus()
    }

    private fun getObservers() {
        lifecycleScope.launch(Dispatchers.Main) {
            settingViewModel.addNumberDataFlow.collect { data ->
                closeSheetValue.value = true
                if (data.message.equals(NUMBER_REGISTERED)) {
                    toast(getString(R.string.number_registered))
                    // refresh the /me api
                    settingViewModel.getCurrentStatus()
                }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            settingViewModel.cancelNumberDataFlow.collect { data ->
                if (data.message.equals(NUMBER_CANCELLED)) {
                    toast(getString(R.string.number_cancelled))
                    // refresh the /me api
                    settingViewModel.getCurrentStatus()
                }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            settingViewModel.currentStatusDataFlow.collect { data ->
                prefs.isOneNumberAdded = data.numbers.isNotEmpty()
                prefs.subscriptionActivated = data.subscriptionActive
                prefs.subscriptionExpireDate = data.subscriptionExpire
                showWhatsappNumbers(data.numbers)
                if (data.subscriptionActive == true && checkAlarmPermission()) {
                    scheduleSubscriptionRenewalReminder(
                        this@SettingsActivity,
                        data.subscriptionExpire ?: ""
                    )
                }
            }
        }
    }

    private fun clickListeners() {
        binding.addWhatsappNumber.setOnClickListener {
            bsAddNumber(settingViewModel.waitForServer) { number ->
                if (prefs.authToken?.isEmpty() == true) {
                    toast(getString(R.string.signIn_before_starting))
                } else {
                    settingViewModel.addNumber(number)
                }
            }
        }
        binding.activeTimeGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.activeInSpecificTime) {
                prefs.monitoringTime24 = false
                binding.specificTimeContainer.beVisible()
                showDaysInRV()
            } else if (checkedId == R.id.alwaysActive) {
                prefs.monitoringTime24 = true
                binding.specificTimeContainer.beGone()
            }
        }
        binding.fromContainer.setOnClickListener {
            timeDropdown(generateTimesList(), from = true)
        }
        binding.toContainer.setOnClickListener {
            timeDropdown(generateTimesList(), from = false)
        }
    }

    private fun getAllApps() {
        lifecycleScope.launch {
            val downloadedApps = settingViewModel.getDownloadedApps(packageManager)
            val filteredApps = downloadedApps.filterNot { it.packageName == packageName }

            filteredApps.forEach { appInfo ->
                appInfo.isSwitchEnabled = prefs.enabledAppList.contains(appInfo.packageName)
            }
            binding.appsRV.layoutManager =
                LinearLayoutManager(this@SettingsActivity, LinearLayoutManager.VERTICAL, false)
            val adapter = AppsAdapter(filteredApps, this@SettingsActivity)
            binding.appsRV.adapter = adapter
            adapter.notifyDataSetChanged()
            binding.appsProgressBar.beGone()
        }
    }

    private fun showWhatsappNumbers(list: List<Numbers>) {
        binding.showNumbersRV.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val adapter = ShowNumbersAdapter(list, this)
        binding.showNumbersRV.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun checkedRadioButton(): Int {
        if (prefs.monitoringTime24 == true) {
            return R.id.alwaysActive
        } else {
            binding.specificTimeContainer.beVisible()
            showDaysInRV()
            return R.id.activeInSpecificTime
        }
    }

    private fun showDaysInRV(){
        val daysOfWeekStrings = resources.getStringArray(R.array.days_of_week).toList()
        daysOfWeek = daysOfWeekStrings.map { Day(day = it) }
        prefs.selectedDaysList.forEach {
            if (it.toInt() < daysOfWeek.size) {
                daysOfWeek[it.toInt()].isSelected = true
            }
        }
        binding.daysRV.layoutManager =
            LinearLayoutManager(
                this@SettingsActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        daysAdapter = DaysAdapter(daysOfWeek, this@SettingsActivity)
        binding.daysRV.adapter = daysAdapter
        daysAdapter.notifyDataSetChanged()
    }

    private fun timeDropdown(timesList: List<String>, from: Boolean) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, timesList)
        AlertDialog.Builder(this)
            .setTitle("Select Time")
            .setAdapter(adapter) { dialog, which ->
                // Handle the selection
                if (from) {
                    binding.fromTime.text = timesList[which]
                    prefs.fromTime = timesList[which]
                } else {
                    binding.toTime.text = timesList[which]
                    prefs.toTime = timesList[which]
                }
            }
            .show()
    }

    override fun enableAppListener(isEnable: Boolean, position: Int, appInfo: AppInfo) {
        val enabledApps = prefs.enabledAppList.toMutableSet()
        if (isEnable) {
            enabledApps.add(appInfo.packageName ?: "")
            toast(getString(R.string.app_selected, appInfo.appName))
        } else {
            enabledApps.remove(appInfo.packageName)
        }
        prefs.enabledAppList = enabledApps
        Log.wtf("apps", prefs.enabledAppList.toString())
    }

    override fun removeNumber(number: String) {
        settingViewModel.cancelNumber(number)
    }

    override fun selectDay(isSelected: Boolean, position: Int) {
        daysOfWeek[position].isSelected = isSelected
        daysAdapter.notifyDataSetChanged()

        val selectedDays = prefs.selectedDaysList.toMutableSet()
        if (isSelected) {
            selectedDays.add(position.toString())
        } else {
            selectedDays.remove(position.toString())
        }

        prefs.selectedDaysList = selectedDays
        Log.wtf("days", prefs.selectedDaysList.toString())
    }


}