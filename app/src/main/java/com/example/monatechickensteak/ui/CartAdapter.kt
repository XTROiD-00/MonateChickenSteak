package com.example.monatechickensteak.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.monatechickensteak.databinding.ItemCartBinding
import com.example.monatechickensteak.model.CartItem
import com.example.monatechickensteak.util.toRand

class CartAdapter(
    private val onPlus: (CartItem) -> Unit,
    private val onMinus: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.VH>(Diff) {

    class VH(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val line = getItem(position)
        with(holder.binding) {
            tvEmoji.text = line.item.emoji
            tvName.text = line.item.name
            tvUnitPrice.text = "${line.item.price.toRand()} each"
            tvQty.text = line.quantity.toString()
            tvLineTotal.text = line.lineTotal.toRand()
            btnPlus.setOnClickListener { onPlus(line) }
            btnMinus.setOnClickListener { onMinus(line) }
            btnRemove.setOnClickListener { onRemove(line) }
        }
    }

    object Diff : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(old: CartItem, new: CartItem) = old.item.id == new.item.id
        override fun areContentsTheSame(old: CartItem, new: CartItem) = old == new
    }
}