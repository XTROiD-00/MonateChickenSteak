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
import com.example.monatechickensteak.data.SessionManager
import com.example.monatechickensteak.databinding.FragmentRegisterBinding
import com.example.monatechickensteak.util.Validators
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.tvBackToLogin.setOnClickListener { findNavController().popBackStack() }

        binding.etName.doAfterTextChanged { binding.tilName.error = null }
        binding.etEmail.doAfterTextChanged { binding.tilEmail.error = null }
        binding.etPassword.doAfterTextChanged { binding.tilPassword.error = null }
        binding.etConfirm.doAfterTextChanged { binding.tilConfirm.error = null }
    }

    private fun attemptRegister() {
        val name = binding.etName.text?.toString().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirm = binding.etConfirm.text?.toString().orEmpty()

        val nameError = Validators.nameError(name)
        val emailError = Validators.emailError(email)
        val passwordError = Validators.newPasswordError(password)
        val confirmError = Validators.confirmPasswordError(password, confirm)

        binding.tilName.error = nameError
        binding.tilEmail.error = emailError
        binding.tilPassword.error = passwordError
        binding.tilConfirm.error = confirmError

        if (listOf(nameError, emailError, passwordError, confirmError).any { it != null }) return

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = AuthRepository.register(name, email, password)) {
                is AuthResult.Success -> {
                    SessionManager.saveSession(result.user.name, result.user.email, result.token)
                    findNavController().navigate(R.id.action_register_to_home)
                }
                is AuthResult.Error -> {
                    setLoading(false)
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}