package com.example.monatechickensteak.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.monatechickensteak.databinding.ItemFoodBinding
import com.example.monatechickensteak.model.FoodItem
import com.example.monatechickensteak.util.toRand

class FoodAdapter(
    private val onAdd: (FoodItem) -> Unit
) : ListAdapter<FoodItem, FoodAdapter.VH>(Diff) {

    class VH(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvEmoji.text = item.emoji
            tvCategory.text = item.category
            tvName.text = item.name
            tvDescription.text = item.description
            tvPrice.text = item.price.toRand()
            btnAdd.setOnClickListener { onAdd(item) }
        }
    }

    object Diff : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(old: FoodItem, new: FoodItem) = old.id == new.id
        override fun areContentsTheSame(old: FoodItem, new: FoodItem) = old == new
    }
}