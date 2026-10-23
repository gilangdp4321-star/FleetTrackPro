package com.industri.fleettrack.ui.manifest

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.industri.fleettrack.data.local.entity.DeliveryOrderEntity
import com.industri.fleettrack.databinding.ItemDeliveryOrderBinding
import java.text.NumberFormat
import java.util.Locale

class DeliveryListAdapter(
    private val onDeliverClicked: (DeliveryOrderEntity) -> Unit
) : ListAdapter<DeliveryOrderEntity, DeliveryListAdapter.DeliveryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val binding = ItemDeliveryOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeliveryViewHolder(binding, onDeliverClicked)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DeliveryViewHolder(
        private val binding: ItemDeliveryOrderBinding,
        private val onDeliverClicked: (DeliveryOrderEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeliveryOrderEntity) {
            binding.tvTrackingNo.text = item.trackingNumber
            binding.tvRecipient.text = "${item.recipientName} (${item.recipientPhone})"
            binding.tvAddress.text = item.destinationAddress

            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvCodAmount.text = currencyFormat.format(item.codAmount)

            val statusUpper = item.deliveryStatus.uppercase(Locale.getDefault())
            binding.tvStatusBadge.text = statusUpper

            if (statusUpper == "DELIVERED" || statusUpper == "TERKIRIM") {
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#E8F5E9"))
                binding.tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"))
                binding.btnActionDeliver.text = "Terkirim / Selesai"
                binding.btnActionDeliver.isEnabled = false
                binding.btnActionDeliver.alpha = 0.6f
            } else {
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#FFF3E0"))
                binding.tvStatusBadge.setTextColor(Color.parseColor("#E65100"))
                binding.btnActionDeliver.text = "Tandai Terkirim"
                binding.btnActionDeliver.isEnabled = true
                binding.btnActionDeliver.alpha = 1.0f
                binding.btnActionDeliver.setOnClickListener {
                    onDeliverClicked(item)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DeliveryOrderEntity>() {
        override fun areItemsTheSame(
            oldItem: DeliveryOrderEntity,
            newItem: DeliveryOrderEntity
        ): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(
            oldItem: DeliveryOrderEntity,
            newItem: DeliveryOrderEntity
        ): Boolean {
            return oldItem == newItem
        }
    }
}
