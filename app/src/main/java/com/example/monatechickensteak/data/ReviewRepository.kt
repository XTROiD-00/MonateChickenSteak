package com.example.monatechickensteak.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.monatechickensteak.model.Review
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** MOCK review box. Will call the API later; the screens will not change. */
object ReviewRepository {

    private const val FIRST_ID = 100

    private val _reviews = MutableLiveData<List<Review>>(seed())
    val reviews: LiveData<List<Review>> = _reviews

    private var nextId = FIRST_ID

    private fun seed(): List<Review> = listOf(
        Review(3, "Malva Pudding", 5, "Best malva in town.", "Thabo N.", "05 Oct 2026"),
        Review(2, "Chips", 4, "Crispy, could use a bit more salt.", "Lerato K.", "03 Oct 2026"),
        Review(1, "Quarter Chicken", 5, "Juicy and full of flavour!", "Sipho M.", "02 Oct 2026")
    )

    fun add(itemName: String, rating: Int, comment: String, author: String): Review {
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val review = Review(nextId++, itemName, rating, comment, author, date)
        _reviews.value = listOf(review) + (_reviews.value ?: emptyList())
        return review
    }

    fun averageRating(): Double {
        val list = _reviews.value.orEmpty()
        return if (list.isEmpty()) 0.0 else list.map { it.rating }.average()
    }

    fun reset() {
        _reviews.value = emptyList()
        nextId = FIRST_ID
    }
}