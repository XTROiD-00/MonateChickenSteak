package com.example.monatechickensteak

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.monatechickensteak.data.OrderRepository
import com.example.monatechickensteak.model.CartItem
import com.example.monatechickensteak.model.FoodItem
import com.example.monatechickensteak.model.OrderStatus
import com.example.monatechickensteak.util.CheckoutValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CheckoutValidatorTest {

    @Test fun emptyCartIsRejected() =
        assertNotNull(CheckoutValidator.error(cartIsEmpty = true, paymentMethod = "Cash on Collection"))

    @Test fun missingPaymentIsRejected() =
        assertNotNull(CheckoutValidator.error(cartIsEmpty = false, paymentMethod = null))

    @Test fun validCheckoutPasses() =
        assertNull(CheckoutValidator.error(cartIsEmpty = false, paymentMethod = "Card Payment"))
}

class OrderRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val chicken = FoodItem(1, "Quarter Chicken", "Chicken", "desc", 75.0, "🍗")
    private val items = listOf(CartItem(chicken, 2))

    @Before
    fun setUp() {
        OrderRepository.reset()
    }

    @Test fun newOrderStartsAsReceived() {
        val order = OrderRepository.placeOrder(items, 175.0, "As soon as possible", "Cash on Collection")
        assertEquals(OrderStatus.RECEIVED, order.status)
        assertEquals(1001, order.id)
    }

    @Test fun orderIdsIncreaseAndNewestIsFirst() {
        OrderRepository.placeOrder(items, 175.0, "In 15 minutes", "Cash on Collection")
        val second = OrderRepository.placeOrder(items, 175.0, "In 30 minutes", "Card Payment")
        val list = OrderRepository.orders.value!!
        assertEquals(2, list.size)
        assertEquals(second.id, list.first().id)
        assertEquals(1002, second.id)
    }

    @Test fun orderKeepsTheItemsAndTotal() {
        val order = OrderRepository.placeOrder(items, 175.0, "In 1 hour", "Card Payment")
        assertEquals(175.0, order.total, 0.001)
        assertEquals(2, order.items.first().quantity)
    }
}