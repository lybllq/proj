package com.example.myapplication.model

import java.io.Serializable

data class Address(
    var id: String = "",
    var name: String = "",
    var phone: String = "",
    var province: String = "",
    var city: String = "",
    var district: String = "",
    var detailAddress: String = "",
    var label: String = "Home",
    var isDefault: Boolean = false
) : Serializable {
    fun getFullAddress(): String = "$province$city$district$detailAddress"
}
