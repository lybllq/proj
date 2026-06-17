package com.example.myapplication.util

import android.content.Context
import com.example.myapplication.model.Order

class OrderManager private constructor() {
    private val orders: MutableList<Order> = mutableListOf()

    fun addOrder(context: Context, order: Order): Result<Unit> {
        val result = runCatching { BackendApi.createOrder(context.applicationContext, order) }
        if (result.isSuccess) {
            refreshFromBackend(context)
        }
        return result
    }

    fun getOrders(context: Context): MutableList<Order> {
        refreshFromBackend(context)
        return orders
    }

    fun getOrderById(context: Context, orderId: Int): Order? {
        refreshFromBackend(context)
        return orders.find { it.id == orderId }
    }

    fun clearOrders() {
        orders.clear()
    }

    fun getOrderCount(): Int = orders.size

    private fun refreshFromBackend(context: Context) {
        runCatching {
            val remote = BackendApi.getOrders(context.applicationContext)
            orders.clear()
            orders.addAll(remote)
        }
    }

    companion object {
        @Volatile
        private var instance: OrderManager? = null

        @JvmStatic
        fun getInstance(): OrderManager =
            instance ?: synchronized(this) {
                instance ?: OrderManager().also { instance = it }
            }
    }
}
