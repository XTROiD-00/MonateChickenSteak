package com.example.monatechickensteak.data

import com.example.monatechickensteak.model.FoodItem

object MockData {

    val categories = listOf(
        "All", "Burgers", "Chicken", "Desserts", "Drinks",
        "Pizza", "Sides", "Steak", "Wings", "Wraps"
    )

    val menu = listOf(
        FoodItem(1, "Quarter Chicken", "Chicken", "Flame-grilled chicken with your choice of side.", 75.0, "🍗"),
        FoodItem(2, "Rump 300g", "Steak", "Juicy rump steak served with a side.", 180.0, "🥩"),
        FoodItem(3, "6 Wings", "Wings", "Peri-peri wings served with a side.", 95.0, "🍖"),
        FoodItem(4, "Cheese Burger", "Burgers", "Beef patty, cheese, lettuce, tomato & sauce.", 55.0, "🍔"),
        FoodItem(5, "Malva Pudding", "Desserts", "Traditional Malva pudding & custard.", 28.0, "🍮"),
        FoodItem(6, "Coca Cola 330ml", "Drinks", "Refreshing Coca Cola soft drink.", 20.0, "🥤"),
        FoodItem(7, "Sprite 330ml", "Drinks", "Refreshing Sprite soft drink.", 20.0, "🥤"),
        FoodItem(8, "Still Water 500ml", "Drinks", "Bottled still water.", 15.0, "💧"),
        FoodItem(9, "Pap & Gravy", "Sides", "Traditional pap with tasty gravy.", 20.0, "🍲"),
        FoodItem(10, "Chips", "Sides", "Crispy golden fried chips.", 25.0, "🍟"),
        FoodItem(11, "Margherita Pizza", "Pizza", "Cheese & tomato classic pizza.", 85.0, "🍕"),
        FoodItem(12, "Chicken Wrap", "Wraps", "Grilled chicken with lettuce & sauce.", 65.0, "🌯")
    )

    fun findById(id: Int): FoodItem? = menu.firstOrNull { it.id == id }
}