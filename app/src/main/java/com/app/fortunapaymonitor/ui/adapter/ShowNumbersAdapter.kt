package com.app.fortunapaymonitor.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.fortunapaymonitor.databinding.NumberItemBinding
import com.app.fortunapaymonitor.data.model.Numbers
import com.app.fortunapaymonitor.ui.clicklistener.RemoveNumberListener
import com.app.fortunapaymonitor.ui.viewholder.ShowNumberViewHolder

class ShowNumbersAdapter(
    private val data: List<Numbers>,
    private val clickListener: RemoveNumberListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ShowNumberViewHolder(getView(parent))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ShowNumberViewHolder).onBind(data[position], clickListener, position)
    }

    override fun getItemCount(): Int = data.size

    private fun getView(parent: ViewGroup?): NumberItemBinding {
        return NumberItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
    }
}