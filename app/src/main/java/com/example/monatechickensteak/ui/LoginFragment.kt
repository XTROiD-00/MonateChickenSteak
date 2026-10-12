package com.example.monatechickensteak.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.monatechickensteak.R
import com.example.monatechickensteak.data.AuthRepository
import com.example.monatechickensteak.data.AuthResult
import com.example.monatechickensteak.data.SessionManager
import com.example.monatechickensteak.databinding.FragmentLoginBinding
import com.example.monatechickensteak.util.Validators
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) return@registerForActivityResult
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                Snackbar.make(binding.root, "Google ID token missing. Check the Web client ID configuration.", Snackbar.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            setLoading(true)
            viewLifecycleOwner.lifecycleScope.launch {
                when (val auth = AuthRepository.googleLogin(idToken)) {
                    is AuthResult.Success -> {
                        SessionManager.saveSession(auth.user.name, auth.user.email, auth.token)
                        findNavController().navigate(R.id.action_login_to_home)
                    }
                    is AuthResult.Error -> { setLoading(false); Snackbar.make(binding.root, auth.message, Snackbar.LENGTH_LONG).show() }
                }
            }
        } catch (e: ApiException) {
            Snackbar.make(binding.root, "Google sign-in failed: ${e.statusCode}", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.btnGoogleSignIn.setOnClickListener { startGoogleSignIn() }
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        // Clear the red error as soon as the user starts fixing it
        binding.etEmail.doAfterTextChanged { binding.tilEmail.error = null }
        binding.etPassword.doAfterTextChanged { binding.tilPassword.error = null }
    }

    private fun startGoogleSignIn() {
        val webClientId = getString(R.string.google_web_client_id)
        if (webClientId.startsWith("YOUR_")) {
            Snackbar.make(binding.root, "Configure google_web_client_id in res/values/strings.xml first.", Snackbar.LENGTH_LONG).show()
            return
        }
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(webClientId)
            .build()
        googleSignInLauncher.launch(GoogleSignIn.getClient(requireActivity(), options).signInIntent)
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        val emailError = Validators.emailError(email)
        val passwordError = Validators.loginPasswordError(password)
        binding.tilEmail.error = emailError
        binding.tilPassword.error = passwordError
        if (emailError != null || passwordError != null) return

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = AuthRepository.login(email, password)) {
                is AuthResult.Success -> {
                    SessionManager.saveSession(result.user.name, result.user.email, result.token)
                    findNavController().navigate(R.id.action_login_to_home)
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
        binding.btnLogin.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}