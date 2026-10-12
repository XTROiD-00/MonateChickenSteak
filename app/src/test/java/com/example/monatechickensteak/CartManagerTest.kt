package com.example.monatechickensteak

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.monatechickensteak.data.CartManager
import com.example.monatechickensteak.model.FoodItem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CartManagerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val chicken = FoodItem(1, "Quarter Chicken", "Chicken", "desc", 75.0, "🍗")
    private val chips = FoodItem(2, "Chips", "Sides", "desc", 25.0, "🍟")

    @Before
    fun setUp() {
        CartManager.clear()
    }

    @Test
    fun addingSameItemTwiceIncreasesQuantity() {
        CartManager.add(chicken)
        CartManager.add(chicken)
        assertEquals(2, CartManager.itemCount())
        assertEquals(1, CartManager.items.value?.size)
    }

    @Test
    fun totalIncludesDeliveryFee() {
        CartManager.add(chicken)
        CartManager.add(chips)
        assertEquals(100.0, CartManager.subtotal(), 0.001)
        assertEquals(125.0, CartManager.total(), 0.001)
    }

    @Test
    fun decreasingToZeroRemovesTheItem() {
        CartManager.add(chips)
        CartManager.decrease(chips.id)
        assertEquals(0, CartManager.itemCount())
    }

    @Test
    fun emptyCartHasNoDeliveryFee() {
        assertEquals(0.0, CartManager.total(), 0.001)
    }
}