package com.app.fortunapaymonitor.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.fortunapaymonitor.databinding.DaysItemBinding
import com.app.fortunapaymonitor.data.model.Day
import com.app.fortunapaymonitor.ui.clicklistener.DaySelectionListener
import com.app.fortunapaymonitor.ui.viewholder.DaysViewHolder

class DaysAdapter(
    private val data: List<Day>,
    private val clickListener: DaySelectionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DaysViewHolder(getView(parent))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as DaysViewHolder).onBind(data[position], clickListener, position)
    }

    override fun getItemCount(): Int = data.size

    private fun getView(parent: ViewGroup?): DaysItemBinding {
        return DaysItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
    }
}