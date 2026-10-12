package com.example.monatechickensteak.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.monatechickensteak.model.CartItem
import com.example.monatechickensteak.model.Order
import com.example.monatechickensteak.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** The step that follows this one, or null when the order is already delivered. */
fun OrderStatus.next(): OrderStatus? = when (this) {
    OrderStatus.RECEIVED -> OrderStatus.PREPARING
    OrderStatus.PREPARING -> OrderStatus.READY
    OrderStatus.READY -> OrderStatus.DELIVERED
    OrderStatus.DELIVERED -> null
}

/**
 * Local order list used to render the current session after the server accepts an order.
 * Server-side order records are authoritative; full server-side order-history refresh is a follow-up.
 */
object OrderRepository {

    private const val FIRST_ORDER_ID = 1001

    private val _orders = MutableLiveData<List<Order>>(seedOrders())
    val orders: LiveData<List<Order>> = _orders

    private var nextId = FIRST_ORDER_ID

    // Two past orders so the history screen has something to show.
    private fun seedOrders(): List<Order> = listOf(
        Order(
            id = 902,
            items = listOf(CartItem(MockData.menu[1], 1), CartItem(MockData.menu[5], 2)),
            total = 245.0,
            collectionTime = "In 30 minutes",
            paymentMethod = "Card Payment",
            status = OrderStatus.DELIVERED,
            placedAt = "04 Oct 2026, 13:05"
        ),
        Order(
            id = 901,
            items = listOf(CartItem(MockData.menu[0], 1), CartItem(MockData.menu[9], 1)),
            total = 125.0,
            collectionTime = "As soon as possible",
            paymentMethod = "Cash on Collection",
            status = OrderStatus.DELIVERED,
            placedAt = "01 Oct 2026, 18:20"
        )
    )

    fun placeOrder(
        items: List<CartItem>,
        total: Double,
        collectionTime: String,
        paymentMethod: String,
        serverOrderId: Int? = null
    ): Order {
        val placedAt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        val order = Order(
            id = serverOrderId ?: nextId++,
            items = items,
            total = total,
            collectionTime = collectionTime,
            paymentMethod = paymentMethod,
            status = OrderStatus.RECEIVED,
            placedAt = placedAt
        )
        // newest order goes to the top of the list
        _orders.value = listOf(order) + (_orders.value ?: emptyList())
        return order
    }

    /** Moves one order to its next status. Orders already delivered are left alone. */
    fun advanceStatus(orderId: Int) {
        _orders.value = (_orders.value ?: emptyList()).map { order ->
            val next = order.status.next()
            if (order.id == orderId && next != null) order.copy(status = next) else order
        }
    }

    fun reset() {
        _orders.value = emptyList()
        nextId = FIRST_ORDER_ID
    }
}