package com.example.monatechickensteak.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.monatechickensteak.model.CartItem
import com.example.monatechickensteak.model.FoodItem

object CartManager {

    const val DELIVERY_FEE = 25.0

    private val _items = MutableLiveData<List<CartItem>>(emptyList())
    val items: LiveData<List<CartItem>> = _items

    private fun current(): List<CartItem> = _items.value ?: emptyList()

    fun add(food: FoodItem) {
        val list = current().toMutableList()
        val index = list.indexOfFirst { it.item.id == food.id }
        if (index >= 0) {
            list[index] = list[index].copy(quantity = list[index].quantity + 1)
        } else {
            list.add(CartItem(food, 1))
        }
        _items.value = list
    }

    fun increase(foodId: Int) {
        _items.value = current().map {
            if (it.item.id == foodId) it.copy(quantity = it.quantity + 1) else it
        }
    }

    fun decrease(foodId: Int) {
        _items.value = current().mapNotNull {
            when {
                it.item.id != foodId -> it
                it.quantity > 1 -> it.copy(quantity = it.quantity - 1)
                else -> null // quantity would hit 0, so remove the line
            }
        }
    }

    fun remove(foodId: Int) {
        _items.value = current().filter { it.item.id != foodId }
    }

    fun clear() {
        _items.value = emptyList()
    }

    fun itemCount(): Int = current().sumOf { it.quantity }

    fun subtotal(): Double = current().sumOf { it.lineTotal }

    fun deliveryFee(): Double = if (current().isEmpty()) 0.0 else DELIVERY_FEE

    fun total(): Double = subtotal() + deliveryFee()
}