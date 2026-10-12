package com.example.monatechickensteak.util

object CheckoutValidator {

    // Returns an error message, or null when the order can be placed.
    fun error(cartIsEmpty: Boolean, paymentMethod: String?): String? = when {
        cartIsEmpty -> "Your cart is empty"
        paymentMethod.isNullOrBlank() -> "Choose a payment method"
        else -> null
    }
}