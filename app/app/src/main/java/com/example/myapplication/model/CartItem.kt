package com.example.myapplication.model

data class CartItem(
    var food: Food,
    var quantity: Int,
    var selected: Boolean = true
) {
    fun getTotalPrice(): Float = food.price * quantity
}
