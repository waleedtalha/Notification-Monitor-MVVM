package com.app.fortunapaymonitor.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.app.fortunapaymonitor.databinding.AppItemBinding
import com.app.fortunapaymonitor.data.model.AppInfo
import com.app.fortunapaymonitor.ui.clicklistener.EnableAppListener

class AppViewHolder(val binding: AppItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(dataModel: AppInfo, clickListener: EnableAppListener, position: Int) {
        binding.appName.text = dataModel.appName
        binding.appIcon.setImageDrawable(dataModel.appIcon)
        binding.enableMonitoring.setOnCheckedChangeListener(null) // Clear any previous listener
        binding.enableMonitoring.isChecked = dataModel.isSwitchEnabled?:false // Set the switch state based on the data model
        binding.enableMonitoring.setOnCheckedChangeListener { _, isChecked ->
            dataModel.isSwitchEnabled = isChecked // Update the state in the data model
            clickListener.enableAppListener(
                isEnable = isChecked,
                position = position,
                appInfo = dataModel
            )
        }
    }
}
