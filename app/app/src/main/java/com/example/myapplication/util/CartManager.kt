package com.example.myapplication.util

import com.example.myapplication.model.CartItem
import com.example.myapplication.model.Food

class CartManager private constructor() {
    private val cartItems: MutableList<CartItem> = mutableListOf()

    fun addToCart(food: Food) {
        val target = cartItems.find { it.food.id == food.id }
        if (target == null) {
            cartItems.add(CartItem(food, 1))
        } else {
            target.quantity += 1
        }
    }

    fun removeFromCart(food: Food) {
        cartItems.removeAll { it.food.id == food.id }
    }

    fun updateQuantity(food: Food, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(food)
            return
        }
        cartItems.find { it.food.id == food.id }?.quantity = quantity
    }

    fun getTotalPrice(): Float = cartItems.filter { it.selected }.sumOf { it.getTotalPrice().toDouble() }.toFloat()

    fun getTotalItems(): Int = cartItems.sumOf { it.quantity }

    fun getCartItems(): MutableList<CartItem> = cartItems

    fun clearCart() {
        cartItems.clear()
    }

    companion object {
        @Volatile
        private var instance: CartManager? = null

        @JvmStatic
        fun getInstance(): CartManager =
            instance ?: synchronized(this) {
                instance ?: CartManager().also { instance = it }
            }
    }
}
