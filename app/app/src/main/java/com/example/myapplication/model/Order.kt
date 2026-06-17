package com.example.myapplication.model

import java.util.Date

data class Order(
    var id: Int,
    var items: List<CartItem>,
    var totalAmount: Float,
    var status: String,
    var orderTime: Date,
    var address: String,
    var userName: String? = null,
    var userPhone: String? = null
) {
    fun isCurrentOrder(): Boolean = status != STATUS_COMPLETED && status != STATUS_CANCELLED

    fun isHistoryOrder(): Boolean = status == STATUS_COMPLETED || status == STATUS_CANCELLED

    companion object {
        const val STATUS_PENDING = "Pending"
        const val STATUS_PAID = "Paid"
        const val STATUS_PREPARING = "Preparing"
        const val STATUS_DELIVERING = "Delivering"
        const val STATUS_COMPLETED = "Completed"
        const val STATUS_CANCELLED = "Cancelled"
    }
}
