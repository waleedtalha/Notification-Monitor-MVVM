package com.app.fortunapaymonitor.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.fortunapaymonitor.databinding.AppItemBinding
import com.app.fortunapaymonitor.data.model.AppInfo
import com.app.fortunapaymonitor.ui.clicklistener.EnableAppListener
import com.app.fortunapaymonitor.ui.viewholder.AppViewHolder

class AppsAdapter(
    private val data: List<AppInfo>,
    private val clickListener: EnableAppListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AppViewHolder(getView(parent))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AppViewHolder).onBind(data[position], clickListener, position)
    }

    override fun getItemCount(): Int = data.size

    private fun getView(parent: ViewGroup?): AppItemBinding {
        return AppItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
    }
}
