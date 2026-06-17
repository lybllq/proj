package com.example.myapplication.model

data class Restaurant(
    var id: Int,
    var name: String,
    var description: String,
    var rating: Float,
    var deliveryTime: Int,
    var deliveryFee: Float,
    var minOrder: Float,
    var imageUrl: String,
    var foods: MutableList<Food> = mutableListOf()
)
