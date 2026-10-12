package com.example.monatechickensteak.util

object LoyaltyRules {

    const val RAND_PER_POINT = 10
    const val POINTS_PER_REVIEW = 5

    data class Reward(val id: Int, val name: String, val cost: Int, val emoji: String)

    val rewards = listOf(
        Reward(1, "Free drink", 50, "🥤"),
        Reward(2, "Free chips", 80, "🍟"),
        Reward(3, "R50 off your order", 200, "💸")
    )

    enum class Tier(val label: String, val minPoints: Int) {
        BRONZE("Bronze", 0),
        SILVER("Silver", 100),
        GOLD("Gold", 300)
    }

    /** 1 point for every R10 spent (rounded down). */
    fun pointsForOrder(total: Double): Int = (total / RAND_PER_POINT).toInt()

    fun tierFor(points: Int): Tier = Tier.values().last { points >= it.minPoints }

    /** The next tier up, or null when the customer is already Gold. */
    fun nextTier(points: Int): Tier? = Tier.values().firstOrNull { it.minPoints > points }
}