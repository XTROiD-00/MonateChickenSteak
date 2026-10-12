package com.example.monatechickensteak

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.monatechickensteak.data.OrderRepository
import com.example.monatechickensteak.data.next
import com.example.monatechickensteak.model.CartItem
import com.example.monatechickensteak.model.FoodItem
import com.example.monatechickensteak.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OrderTrackingTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val chips = FoodItem(10, "Chips", "Sides", "desc", 25.0, "🍟")
    private val items = listOf(CartItem(chips, 1))

    @Before
    fun setUp() {
        OrderRepository.reset()
    }

    @Test fun statusFollowsTheKitchenFlow() {
        assertEquals(OrderStatus.PREPARING, OrderStatus.RECEIVED.next())
        assertEquals(OrderStatus.READY, OrderStatus.PREPARING.next())
        assertEquals(OrderStatus.DELIVERED, OrderStatus.READY.next())
    }

    @Test fun deliveredHasNoNextStatus() = assertNull(OrderStatus.DELIVERED.next())

    @Test fun advanceStatusMovesTheOrderForward() {
        val order = OrderRepository.placeOrder(items, 50.0, "As soon as possible", "Cash on Collection")
        OrderRepository.advanceStatus(order.id)
        val updated = OrderRepository.orders.value!!.first { it.id == order.id }
        assertEquals(OrderStatus.PREPARING, updated.status)
    }

    @Test fun advancingPastDeliveredChangesNothing() {
        val order = OrderRepository.placeOrder(items, 50.0, "In 15 minutes", "Card Payment")
        repeat(6) { OrderRepository.advanceStatus(order.id) }
        val updated = OrderRepository.orders.value!!.first { it.id == order.id }
        assertEquals(OrderStatus.DELIVERED, updated.status)
    }
}