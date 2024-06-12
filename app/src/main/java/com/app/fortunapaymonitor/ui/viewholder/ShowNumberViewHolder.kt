package com.app.fortunapaymonitor.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.app.fortunapaymonitor.databinding.NumberItemBinding
import com.app.fortunapaymonitor.utils.extensions.formatAsPhoneNumber
import com.app.fortunapaymonitor.data.model.Numbers
import com.app.fortunapaymonitor.ui.clicklistener.RemoveNumberListener

class ShowNumberViewHolder(val binding: NumberItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(dataModel: Numbers, clickListener: RemoveNumberListener, position: Int) {
        binding.whatsappNumber.text = dataModel.number?.formatAsPhoneNumber()
        binding.removeNumber.setOnClickListener {
            clickListener.removeNumber(dataModel.number.toString())
        }
    }
}