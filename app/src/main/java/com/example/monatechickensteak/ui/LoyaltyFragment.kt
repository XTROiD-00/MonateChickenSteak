package com.example.monatechickensteak.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.monatechickensteak.data.AuthRepository
import com.example.monatechickensteak.data.SessionManager
import com.example.monatechickensteak.databinding.FragmentLoyaltyBinding
import com.example.monatechickensteak.databinding.ItemRewardBinding
import com.example.monatechickensteak.util.LoyaltyRules
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class LoyaltyFragment : Fragment() {

    private var _binding: FragmentLoyaltyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoyaltyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render()
    }

    private fun render() {
        val points = AuthRepository.loyaltyPoints(SessionManager.userEmail())
        val tier = LoyaltyRules.tierFor(points)
        val next = LoyaltyRules.nextTier(points)

        binding.tvPoints.text = points.toString()
        binding.tvTier.text = "${tier.label} member"

        if (next == null) {
            binding.progress.setProgressCompat(100, true)
            binding.tvNext.text = "You've reached the top tier! 🏆"
        } else {
            val percent = (points - tier.minPoints) * 100 / (next.minPoints - tier.minPoints)
            binding.progress.setProgressCompat(percent, true)
            binding.tvNext.text = "${next.minPoints - points} more points to reach ${next.label}"
        }

        binding.rewardsContainer.removeAllViews()
        LoyaltyRules.rewards.forEach { reward ->
            val row = ItemRewardBinding.inflate(layoutInflater, binding.rewardsContainer, false)
            row.tvEmoji.text = reward.emoji
            row.tvRewardName.text = reward.name
            row.tvCost.text = "${reward.cost} points"
            row.btnRedeem.isEnabled = points >= reward.cost
            row.btnRedeem.setOnClickListener { redeem(reward) }
            binding.rewardsContainer.addView(row.root)
        }
    }

    private fun redeem(reward: LoyaltyRules.Reward) {
        val ok = AuthRepository.redeemPoints(SessionManager.userEmail(), reward.cost)
        if (!ok) {
            Snackbar.make(binding.root, "Not enough points yet", Snackbar.LENGTH_SHORT).show()
            return
        }
        val code = "MON-${(1000..9999).random()}"
        render()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reward unlocked! 🎁")
            .setMessage("${reward.name}\n\nShow this code at the till:\n$code")
            .setPositiveButton("Great!", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}