package com.example.myapplication.util

import android.content.Context

object BackendConfig {
    private const val PREFS_NAME = "backend_config"
    private const val KEY_BASE_URL = "base_url"
    const val DEFAULT_BASE_URL = "http://10.0.2.2:3001"
    private const val ADB_REVERSE_BASE_URL = "http://127.0.0.1:3001"

    fun getBaseUrl(context: Context): String {
        clearSavedBaseUrl(context)
        return DEFAULT_BASE_URL
    }

    fun getBaseUrls(context: Context): List<String> {
        clearSavedBaseUrl(context)
        return listOf(DEFAULT_BASE_URL, ADB_REVERSE_BASE_URL)
    }

    private fun clearSavedBaseUrl(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_BASE_URL)
            .apply()
    }

    @Suppress("UNUSED_PARAMETER")
    fun saveBaseUrl(context: Context, baseUrl: String) {
        clearSavedBaseUrl(context)
    }
}
