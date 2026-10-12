package com.example.monatechickensteak.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.monatechickensteak.R
import com.example.monatechickensteak.data.AuthRepository
import com.example.monatechickensteak.data.AuthResult
import com.example.monatechickensteak.data.CartManager
import com.example.monatechickensteak.data.SessionManager
import com.example.monatechickensteak.databinding.FragmentProfileBinding
import com.example.monatechickensteak.util.Validators
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshHeader()

        // Settings switch (set the starting value BEFORE attaching the listener)
        binding.switchNotifications.isChecked = SessionManager.notificationsEnabled()
        binding.switchNotifications.setOnCheckedChangeListener { _, checked ->
            SessionManager.setNotificationsEnabled(checked)
            snack(if (checked) "Order notifications turned on" else "Order notifications turned off")
        }

        binding.btnSaveName.setOnClickListener { saveName() }
        binding.btnChangePassword.setOnClickListener { changePassword() }
        binding.btnLogout.setOnClickListener { confirmLogout() }
        binding.btnLoyalty.setOnClickListener { findNavController().navigate(R.id.loyaltyFragment) }
        binding.btnReviews.setOnClickListener { findNavController().navigate(R.id.reviewsFragment) }
        binding.btnSupport.setOnClickListener { findNavController().navigate(R.id.supportFragment) }
        binding.etName.doAfterTextChanged { binding.tilName.error = null }
        binding.etCurrent.doAfterTextChanged { binding.tilCurrent.error = null }
        binding.etNew.doAfterTextChanged { binding.tilNew.error = null }
        binding.etConfirm.doAfterTextChanged { binding.tilConfirm.error = null }
    }

    private fun refreshHeader() {
        val name = SessionManager.userName()
        val email = SessionManager.userEmail()
        binding.tvName.text = name
        binding.tvEmail.text = email
        binding.tvAvatar.text = name.trim().firstOrNull()?.uppercase() ?: "?"
        binding.tvPoints.text = AuthRepository.loyaltyPoints(email).toString()
        binding.etName.setText(name)
    }

    private fun saveName() {
        val name = binding.etName.text?.toString().orEmpty()
        val error = Validators.nameError(name)
        binding.tilName.error = error
        if (error != null) return

        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = AuthRepository.updateName(SessionManager.userEmail(), name)) {
                is AuthResult.Success -> {
                    SessionManager.saveSession(result.user.name, result.user.email)
                    refreshHeader()
                    snack("Name updated")
                }
                is AuthResult.Error -> snack(result.message)
            }
        }
    }

    private fun changePassword() {
        val current = binding.etCurrent.text?.toString().orEmpty()
        val newPassword = binding.etNew.text?.toString().orEmpty()
        val confirm = binding.etConfirm.text?.toString().orEmpty()

        val currentError = Validators.loginPasswordError(current)
        val newError = Validators.newPasswordError(newPassword)
        val confirmError = Validators.confirmPasswordError(newPassword, confirm)

        binding.tilCurrent.error = currentError
        binding.tilNew.error = newError
        binding.tilConfirm.error = confirmError
        if (listOf(currentError, newError, confirmError).any { it != null }) return

        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = AuthRepository.changePassword(
                SessionManager.userEmail(), current, newPassword
            )) {
                is AuthResult.Success -> {
                    binding.etCurrent.setText("")
                    binding.etNew.setText("")
                    binding.etConfirm.setText("")
                    snack("Password updated")
                }
                is AuthResult.Error -> {
                    if (result.message.startsWith("Current")) {
                        binding.tilCurrent.error = result.message
                    } else {
                        snack(result.message)
                    }
                }
            }
        }
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log out?")
            .setMessage("You will need to log in again to place orders.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Log out") { _, _ ->
                SessionManager.logout()
                CartManager.clear()
                findNavController().navigate(R.id.action_global_logout)
            }
            .show()
    }

    private fun snack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}