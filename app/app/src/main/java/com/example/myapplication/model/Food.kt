package com.example.myapplication.model

data class Food(
    var id: Int,
    var name: String,
    var description: String,
    var price: Float,
    var imageUrl: String,
    var restaurantId: Int,
    var monthlySales: Int
)
