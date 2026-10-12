package com.example.monatechickensteak.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.monatechickensteak.databinding.ItemReviewBinding
import com.example.monatechickensteak.model.Review

class ReviewAdapter : ListAdapter<Review, ReviewAdapter.VH>(Diff) {

    class VH(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val review = getItem(position)
        with(holder.binding) {
            tvItem.text = review.itemName
            tvStars.text = "★".repeat(review.rating) + "☆".repeat(5 - review.rating)
            tvComment.text = review.comment
            tvMeta.text = "${review.author}  •  ${review.date}"
        }
    }

    object Diff : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(old: Review, new: Review) = old.id == new.id
        override fun areContentsTheSame(old: Review, new: Review) = old == new
    }
}