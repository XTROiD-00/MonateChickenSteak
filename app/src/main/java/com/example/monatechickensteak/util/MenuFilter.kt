package com.example.monatechickensteak.util

import com.example.monatechickensteak.model.FoodItem

object MenuFilter {

    fun filter(items: List<FoodItem>, category: String, query: String): List<FoodItem> {
        val q = query.trim()
        return items.filter { item ->
            val categoryMatch = category == "All" || item.category.equals(category, ignoreCase = true)
            val queryMatch = q.isEmpty() ||
                    item.name.contains(q, ignoreCase = true) ||
                    item.description.contains(q, ignoreCase = true)
            categoryMatch && queryMatch
        }
    }
}