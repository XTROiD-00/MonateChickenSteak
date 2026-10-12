package com.example.monatechickensteak.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.monatechickensteak.databinding.FragmentSupportBinding
import com.example.monatechickensteak.databinding.ItemFaqBinding
import com.google.android.material.snackbar.Snackbar

class SupportFragment : Fragment() {

    private var _binding: FragmentSupportBinding? = null
    private val binding get() = _binding!!

    private val faqs = listOf(
        "How do I track my order?" to
                "Open the Orders tab. The progress bar moves from Received to Preparing, Ready and Delivered as the kitchen updates your order.",
        "Can I change or cancel my order?" to
                "Call us straight away on 012 345 6789. Once an order is Preparing it can no longer be changed.",
        "How do I earn loyalty points?" to
                "You earn 1 point for every R10 you spend and 5 points for every review. Redeem them under Profile > Loyalty & rewards.",
        "Which payment methods do you accept?" to
                "Cash on collection, card payment and other payment options. You choose at checkout.",
        "How do I change my password?" to
                "Go to Profile and use the Change password section. If you cannot log in, contact us and we will help.",
        "What are your opening hours?" to
                "Monday to Thursday 11:00 AM - 10:00 PM\nFriday and Saturday 11:00 AM - 11:00 PM\nSunday 11:00 AM - 9:00 PM"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Build the FAQ list: tap a question to open or close its answer
        faqs.forEach { (question, answer) ->
            val row = ItemFaqBinding.inflate(layoutInflater, binding.faqContainer, false)
            row.tvQuestion.text = question
            row.tvAnswer.text = answer
            row.root.setOnClickListener {
                val open = row.tvAnswer.visibility == View.VISIBLE
                row.tvAnswer.visibility = if (open) View.GONE else View.VISIBLE
                row.tvArrow.text = if (open) "▾" else "▴"
            }
            binding.faqContainer.addView(row.root)
        }

        binding.btnCall.setOnClickListener {
            openIntent(Intent(Intent.ACTION_DIAL, Uri.parse("tel:0123456789")))
        }
        binding.btnEmail.setOnClickListener {
            val email = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:info@monate.co.za")
                putExtra(Intent.EXTRA_SUBJECT, "Monate app support")
            }
            openIntent(email)
        }

        binding.etMessage.doAfterTextChanged { binding.tilMessage.error = null }
        binding.btnSend.setOnClickListener { sendMessage() }
    }

    private fun openIntent(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(binding.root, "No app found to do that on this phone", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage() {
        val message = binding.etMessage.text?.toString().orEmpty().trim()
        if (message.length < 10) {
            binding.tilMessage.error = "Please tell us a bit more (at least 10 characters)"
            return
        }
        // MOCK: the real app will send this to the API
        binding.etMessage.setText("")
        Snackbar.make(binding.root, "Message sent. We'll reply within 24 hours.", Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}