package com.example.monatechickensteak.util

object ReviewValidator {

    fun itemError(item: String?): String? =
        if (item.isNullOrBlank()) "Choose what you are reviewing" else null

    fun ratingError(rating: Int): String? =
        if (rating < 1) "Tap a star rating" else null

    fun commentError(comment: String): String? = when {
        comment.isBlank() -> "Please write a short comment"
        comment.trim().length < 5 -> "Comment is too short"
        comment.length > 300 -> "Keep it under 300 characters"
        else -> null
    }
}