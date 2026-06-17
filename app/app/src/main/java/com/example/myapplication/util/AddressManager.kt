package com.example.myapplication.util

import android.content.Context
import com.example.myapplication.model.Address

class AddressManager private constructor(context: Context) {
    private val appContext = context.applicationContext

    fun getAllAddresses(): MutableList<Address> {
        return runCatching { BackendApi.getAddresses(appContext) }.getOrDefault(mutableListOf())
    }

    fun addAddress(address: Address): Result<Unit> {
        return runCatching { BackendApi.addAddress(appContext, address) }
    }

    fun updateAddress(updatedAddress: Address): Result<Unit> {
        return runCatching { BackendApi.updateAddress(appContext, updatedAddress) }
    }

    fun deleteAddress(addressId: String): Result<Unit> {
        return runCatching { BackendApi.deleteAddress(appContext, addressId) }
    }

    fun getAddressById(id: String): Address? = getAllAddresses().find { it.id == id }

    fun getDefaultAddress(): Address? {
        val addresses = getAllAddresses()
        return addresses.find { it.isDefault } ?: addresses.firstOrNull()
    }

    fun setDefaultAddress(addressId: String): Result<Unit> {
        return runCatching { BackendApi.setDefaultAddress(appContext, addressId) }
    }

    fun clearAllAddresses() {
        getAllAddresses().forEach { runCatching { BackendApi.deleteAddress(appContext, it.id) } }
    }

    companion object {
        @Volatile
        private var instance: AddressManager? = null

        @JvmStatic
        fun getInstance(context: Context): AddressManager =
            instance ?: synchronized(this) {
                instance ?: AddressManager(context).also { instance = it }
            }
    }
}
