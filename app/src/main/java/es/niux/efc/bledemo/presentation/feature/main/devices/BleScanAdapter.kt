package es.niux.efc.bledemo.presentation.feature.main.devices

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import es.niux.efc.bledemo.R
import es.niux.efc.bluetooth.data.source.event.BleScanEvent
import kotlinx.android.synthetic.main.main_devices_item.view.*

class BleScanAdapter : ListAdapter<BleScanEvent.Found, BleScanAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<BleScanEvent.Found>() {
        override fun areItemsTheSame(oldItem: BleScanEvent.Found, newItem: BleScanEvent.Found): Boolean =
            oldItem.device.address == newItem.device.address

        override fun areContentsTheSame(oldItem: BleScanEvent.Found, newItem: BleScanEvent.Found): Boolean =
            oldItem.device.address == newItem.device.address
                    && oldItem.device.name == newItem.device.name
                    && oldItem.rssi == newItem.rssi
    }
) {
    var listenerSelection: ((BleScanEvent.Found) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvAddress = view.tv_main_devices_address!!
        private val tvName = view.tv_main_devices_name!!
        private val tvRSSI = view.tv_main_devices_rssi!!

        @SuppressLint("SetTextI18n")
        fun bind(
            item: BleScanEvent.Found,
            listenerSelection: (BleScanEvent.Found) -> Unit
        ) {
            tvAddress.text = item.device.address
            tvName.text = item.device.name ?: "??"
            tvName.typeface = when {
                item.device.name != null ->
                    Typeface.DEFAULT_BOLD
                else ->
                    Typeface.DEFAULT
            }
            tvRSSI.text = "${item.rssi} RSSI"

            itemView.setOnClickListener { listenerSelection(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.main_devices_item, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position)) { listenerSelection?.invoke(it) }
}
