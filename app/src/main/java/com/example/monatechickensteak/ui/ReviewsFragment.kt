package com.example.monatechickensteak.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monatechickensteak.data.AuthRepository
import com.example.monatechickensteak.data.MockData
import com.example.monatechickensteak.data.ReviewRepository
import com.example.monatechickensteak.data.SessionManager
import com.example.monatechickensteak.databinding.FragmentReviewsBinding
import com.example.monatechickensteak.util.LoyaltyRules
import com.example.monatechickensteak.util.ReviewValidator
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class ReviewsFragment : Fragment() {

    private var _binding: FragmentReviewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReviewAdapter()
        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = adapter

        val choices = listOf("Overall experience") + MockData.menu.map { it.name }
        binding.dropdownItem.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, choices)
        )
        binding.dropdownItem.setText(choices.first(), false)

        binding.etComment.doAfterTextChanged { binding.tilComment.error = null }
        binding.btnSubmit.setOnClickListener { submit() }

        ReviewRepository.reviews.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvAverage.text = if (list.isEmpty()) {
                "No reviews yet. Be the first!"
            } else {
                String.format(
                    Locale.US, "%.1f ★  (%d reviews)",
                    ReviewRepository.averageRating(), list.size
                )
            }
        }
    }

    private fun submit() {
        val item = binding.dropdownItem.text?.toString()
        val rating = binding.ratingBar.rating.toInt()
        val comment = binding.etComment.text?.toString().orEmpty()

        val itemError = ReviewValidator.itemError(item)
        val ratingError = ReviewValidator.ratingError(rating)
        val commentError = ReviewValidator.commentError(comment)

        binding.tilComment.error = commentError
        val firstError = itemError ?: ratingError
        if (firstError != null) {
            Snackbar.make(binding.root, firstError, Snackbar.LENGTH_SHORT).show()
        }
        if (itemError != null || ratingError != null || commentError != null) return

        val author = SessionManager.userName().ifBlank { "Customer" }
        ReviewRepository.add(item!!, rating, comment.trim(), author)
        AuthRepository.addLoyaltyPoints(SessionManager.userEmail(), LoyaltyRules.POINTS_PER_REVIEW)

        binding.ratingBar.rating = 0f
        binding.etComment.setText("")
        Snackbar.make(
            binding.root,
            "Thanks for your review! +${LoyaltyRules.POINTS_PER_REVIEW} loyalty points",
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}