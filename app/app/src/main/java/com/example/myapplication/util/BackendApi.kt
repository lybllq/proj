package com.example.myapplication.util

import android.content.Context
import com.example.myapplication.model.Address
import com.example.myapplication.model.CartItem
import com.example.myapplication.model.Food
import com.example.myapplication.model.Order
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.util.Date

object BackendApi {
    private val gson = Gson()

    data class AuthUser(
        val phone: String,
        val name: String
    )

    private fun executeRequest(url: String, method: String, body: Any? = null): Pair<Int, String> {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5000
            readTimeout = 5000
            useCaches = false
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Connection", "close")
            doInput = true
            if (body != null) doOutput = true
        }
        try {
            if (body != null) {
                OutputStreamWriter(conn.outputStream).use { it.write(gson.toJson(body)) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.let { BufferedReader(it.reader()).use { reader -> reader.readText() } }.orEmpty()
            return code to text
        } finally {
            conn.disconnect()
        }
    }

    private fun requestOnce(baseUrl: String, method: String, path: String, body: Any? = null): String {
        val primaryUrl = "$baseUrl/api$path"
        val (primaryCode, primaryText) = executeRequest(primaryUrl, method, body)
        if (primaryCode in 200..299) return primaryText

        // Some local environments expose backend routes without `/api` prefix.
        if (primaryCode == 404) {
            val fallbackUrl = "$baseUrl$path"
            val (fallbackCode, fallbackText) = executeRequest(fallbackUrl, method, body)
            if (fallbackCode in 200..299) return fallbackText
            throw IllegalStateException("HTTP $fallbackCode: $fallbackText (url=$fallbackUrl)")
        }

        throw IllegalStateException("HTTP $primaryCode: $primaryText (url=$primaryUrl)")
    }

    private fun request(context: Context, method: String, path: String, body: Any? = null): String {
        var lastError: Throwable? = null
        BackendConfig.getBaseUrls(context).forEach { baseUrl ->
            try {
                return requestOnce(baseUrl, method, path, body)
            } catch (error: Throwable) {
                lastError = error
            }
        }

        throw lastError ?: IllegalStateException("Backend request failed")
    }

    private fun normalizedPhone(context: Context): String {
        val raw = UserProfileManager.getInstance(context).getUserPhone()
        val digits = raw.filter { it.isDigit() }
        return if (digits.length in 10..15) digits else "13800008888"
    }

    fun ensureUser(context: Context): String {
        val phone = normalizedPhone(context)
        request(context, "POST", "/auth/upsert", mapOf("phone" to phone))
        return phone
    }

    fun login(context: Context, phone: String, password: String): AuthUser {
        val json = request(
            context,
            "POST",
            "/auth/login",
            mapOf("phone" to phone, "password" to password)
        )
        val root = JsonParser.parseString(json).asJsonObject
        val user = root.getAsJsonObject("user")
        return AuthUser(
            phone = user.get("phone").asString,
            name = user.get("name")?.asString ?: "User"
        )
    }

    fun register(context: Context, phone: String, password: String, name: String? = null): AuthUser {
        val payload = mutableMapOf<String, Any>(
            "phone" to phone,
            "password" to password
        )
        if (!name.isNullOrBlank()) payload["name"] = name
        val json = request(context, "POST", "/auth/register", payload)
        val root = JsonParser.parseString(json).asJsonObject
        val user = root.getAsJsonObject("user")
        return AuthUser(
            phone = user.get("phone").asString,
            name = user.get("name")?.asString ?: "User"
        )
    }

    fun getAddresses(context: Context): MutableList<Address> {
        val phone = ensureUser(context)
        val encoded = URLEncoder.encode(phone, "UTF-8")
        val json = request(context, "GET", "/addresses?userPhone=$encoded")
        val array = JsonParser.parseString(json).asJsonArray
        return array.map { node ->
            val obj = node.asJsonObject
            Address(
                id = obj.get("id").asString,
                name = obj.get("receiver").asString,
                phone = obj.get("phone").asString,
                province = obj.get("province").asString,
                city = obj.get("city").asString,
                district = obj.get("district").asString,
                detailAddress = obj.get("detail").asString,
                label = obj.get("label").asString,
                isDefault = obj.get("isDefault").asBoolean
            )
        }.toMutableList()
    }

    fun getFoods(context: Context): MutableList<Food> {
        val json = request(context, "GET", "/foods")
        val array = JsonParser.parseString(json).asJsonArray
        return array.mapIndexed { index, node ->
            val obj = node.asJsonObject
            val rawId = obj.get("id").asString
            val numericId = rawId.filter { it.isDigit() }.toIntOrNull() ?: (index + 1)
            Food(
                id = numericId,
                name = obj.get("name").asString,
                description = obj.get("category").asString,
                price = obj.get("price").asFloat,
                imageUrl = obj.get("emoji").asString,
                restaurantId = 1,
                monthlySales = 0
            )
        }.toMutableList()
    }

    fun addAddress(context: Context, address: Address) {
        val phone = ensureUser(context)
        request(
            context,
            "POST",
            "/addresses",
            mapOf(
                "userPhone" to phone,
                "receiver" to address.name,
                "phone" to address.phone,
                "province" to address.province,
                "city" to address.city,
                "district" to address.district,
                "detail" to address.detailAddress,
                "fullAddress" to address.getFullAddress(),
                "label" to address.label,
                "isDefault" to address.isDefault
            )
        )
    }

    fun updateAddress(context: Context, address: Address) {
        val phone = ensureUser(context)
        request(
            context,
            "PUT",
            "/addresses/${address.id}",
            mapOf(
                "userPhone" to phone,
                "receiver" to address.name,
                "phone" to address.phone,
                "province" to address.province,
                "city" to address.city,
                "district" to address.district,
                "detail" to address.detailAddress,
                "fullAddress" to address.getFullAddress(),
                "label" to address.label,
                "isDefault" to address.isDefault
            )
        )
    }

    fun deleteAddress(context: Context, addressId: String) {
        val phone = ensureUser(context)
        val encoded = URLEncoder.encode(phone, "UTF-8")
        request(context, "DELETE", "/addresses/$addressId?userPhone=$encoded")
    }

    fun setDefaultAddress(context: Context, addressId: String) {
        val phone = ensureUser(context)
        request(context, "PATCH", "/addresses/$addressId/default", mapOf("userPhone" to phone))
    }

    fun getOrders(context: Context): MutableList<Order> {
        val phone = ensureUser(context)
        val encoded = URLEncoder.encode(phone, "UTF-8")
        val json = request(context, "GET", "/orders?userPhone=$encoded")
        val array = JsonParser.parseString(json).asJsonArray
        return array.map { node ->
            val obj = node.asJsonObject
            val itemsArray = obj.get("items") as JsonArray
            val items = itemsArray.mapIndexed { index, itemNode ->
                val itemObj = itemNode.asJsonObject
                val rawFoodId = itemObj.get("foodId")?.asString ?: "0"
                val foodId = rawFoodId.filter { it.isDigit() }.toIntOrNull() ?: (index + 1)
                val price = itemObj.get("price").asFloat
                CartItem(
                    food = Food(
                        id = foodId,
                        name = itemObj.get("name").asString,
                        description = "",
                        price = price,
                        imageUrl = "",
                        restaurantId = 0,
                        monthlySales = 0
                    ),
                    quantity = itemObj.get("qty").asInt
                )
            }
            Order(
                id = obj.get("id").asInt,
                items = items,
                totalAmount = obj.get("payable").asFloat,
                status = obj.get("status").asString,
                orderTime = Date(obj.get("createdAt").asLong),
                address = obj.get("address").asString,
                userName = obj.get("receiver").asString,
                userPhone = obj.get("phone").asString
            )
        }.toMutableList()
    }

    fun createOrder(context: Context, order: Order) {
        val phone = ensureUser(context)
        val itemMaps = order.items.map {
            mapOf(
                "id" to it.food.id.toString(),
                "name" to it.food.name,
                "qty" to it.quantity,
                "price" to it.food.price,
                "subtotal" to it.getTotalPrice()
            )
        }
        val body = JsonObject().apply {
            addProperty("userPhone", phone)
            add("items", gson.toJsonTree(itemMaps))
            addProperty("goodsAmount", order.totalAmount - 5.0f)
            addProperty("deliveryFee", 5.0f)
            addProperty("couponDiscount", 0.0f)
            addProperty("payable", order.totalAmount)
            addProperty("payMethod", "card")
            addProperty("address", order.address)
            addProperty("receiver", order.userName ?: "User")
            addProperty("receiverPhone", order.userPhone ?: phone)
            addProperty("addressLabel", "Home")
        }
        request(context, "POST", "/orders", body)
    }
}
