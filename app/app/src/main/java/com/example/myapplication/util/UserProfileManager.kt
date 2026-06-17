package com.example.myapplication.util

import android.content.Context

class UserProfileManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val prefs by lazy {
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, DEFAULT_USER_NAME) ?: DEFAULT_USER_NAME

    fun getUserPhone(): String = prefs.getString(KEY_USER_PHONE, DEFAULT_USER_PHONE) ?: DEFAULT_USER_PHONE

    fun saveProfile(userName: String, userPhone: String) {
        prefs.edit()
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_PHONE, userPhone)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "user_profile_prefs"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val DEFAULT_USER_NAME = "User"
        private const val DEFAULT_USER_PHONE = "138****8888"

        @Volatile
        private var instance: UserProfileManager? = null

        @JvmStatic
        fun getInstance(context: Context): UserProfileManager =
            instance ?: synchronized(this) {
                instance ?: UserProfileManager(context).also { instance = it }
            }
    }
}
