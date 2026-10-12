package com.example.monatechickensteak.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.monatechickensteak.R
import com.example.monatechickensteak.databinding.ItemOrderBinding
import com.example.monatechickensteak.model.Order
import com.example.monatechickensteak.model.OrderStatus
import com.example.monatechickensteak.util.toRand

class OrderAdapter(
    private val onAdvance: (Order) -> Unit,
    private val onReorder: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.VH>(Diff) {

    class VH(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val order = getItem(position)
        val context = holder.itemView.context
        val delivered = order.status == OrderStatus.DELIVERED

        with(holder.binding) {
            tvOrderId.text = "Order #${order.id}"
            tvPlacedAt.text = order.placedAt
            tvItems.text = order.items.joinToString("\n") { "${it.quantity} × ${it.item.name}" }
            tvMeta.text = "Collection: ${order.collectionTime}  •  Payment: ${order.paymentMethod}"
            tvTotal.text = order.total.toRand()

            tvStatus.text = order.status.label
            tvStatus.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (delivered) R.color.monate_green else R.color.monate_red
                )
            )

            // Progress bar and the four step labels
            progress.setProgressCompat((order.status.ordinal + 1) * 25, false)
            val steps = listOf(tvStep1, tvStep2, tvStep3, tvStep4)
            OrderStatus.values().forEachIndexed { index, status ->
                steps[index].text = "●\n${status.label}"
                val reached = index <= order.status.ordinal
                steps[index].setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (reached) R.color.monate_red else R.color.monate_grey
                    )
                )
            }

            // Demo button only while the order is still in progress; "Order again" only when done
            btnAdvance.visibility = if (delivered) View.GONE else View.VISIBLE
            btnReorder.visibility = if (delivered) View.VISIBLE else View.GONE
            btnAdvance.setOnClickListener { onAdvance(order) }
            btnReorder.setOnClickListener { onReorder(order) }
        }
    }

    object Diff : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(old: Order, new: Order) = old.id == new.id
        override fun areContentsTheSame(old: Order, new: Order) = old == new
    }
}