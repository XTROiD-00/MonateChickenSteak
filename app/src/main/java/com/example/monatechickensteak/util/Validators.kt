package com.example.monatechickensteak.util

object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Each function returns an error message, or null when the input is fine.

    fun nameError(name: String): String? = when {
        name.isBlank() -> "Name is required"
        name.trim().length < 2 -> "Name is too short"
        else -> null
    }

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !EMAIL_REGEX.matches(email.trim()) -> "Enter a valid email address"
        else -> null
    }

    // Used on the login screen: just make sure something was typed
    fun loginPasswordError(password: String): String? =
        if (password.isEmpty()) "Password is required" else null

    // Used on the register screen: enforce a strong password
    fun newPasswordError(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        password.length < 8 -> "Use at least 8 characters"
        !password.any { it.isUpperCase() } -> "Add an uppercase letter"
        !password.any { it.isLowerCase() } -> "Add a lowercase letter"
        !password.any { it.isDigit() } -> "Add a number"
        password.none { !it.isLetterOrDigit() } -> "Add a special character (e.g. @ # !)"
        else -> null
    }

    fun confirmPasswordError(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Please confirm your password"
        password != confirm -> "Passwords do not match"
        else -> null
    }
}