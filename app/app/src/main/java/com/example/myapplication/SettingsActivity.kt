package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.util.bindBackButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        bindBackButton()
        setupSettings()
    }

    private fun setupSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val switchNotification = findViewById<Switch>(R.id.switch_notification)
        val switchDarkMode = findViewById<Switch>(R.id.switch_dark_mode)
        val btnClearCache = findViewById<Button>(R.id.btn_clear_cache)

        switchNotification.isChecked = prefs.getBoolean(KEY_NOTIFICATION, true)
        switchDarkMode.isChecked = prefs.getBoolean(KEY_DARK_MODE, false)

        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_NOTIFICATION, isChecked).apply()
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply()
            toast(if (isChecked) "Dark mode enabled" else "Dark mode disabled")
        }

        btnClearCache.setOnClickListener {
            toast("Cache cleared")
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_NOTIFICATION = "notification"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}
