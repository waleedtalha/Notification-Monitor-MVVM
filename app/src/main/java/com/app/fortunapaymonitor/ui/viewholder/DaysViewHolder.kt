package com.app.fortunapaymonitor.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.app.fortunapaymonitor.R
import com.app.fortunapaymonitor.databinding.DaysItemBinding
import com.app.fortunapaymonitor.data.model.Day
import com.app.fortunapaymonitor.ui.clicklistener.DaySelectionListener

class DaysViewHolder(val binding: DaysItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(day: Day, clickListener: DaySelectionListener, position: Int) {
        binding.day.text = day.day
        if (day.isSelected == true) {
            binding.day.setBackgroundResource(R.drawable.selected_day_bg)
        } else {
            binding.day.setBackgroundResource(R.drawable.qr_code_bg)
        }

        binding.day.setOnClickListener {
            day.isSelected = day.isSelected != true
            clickListener.selectDay(day.isSelected?:false,position)
        }
    }
}