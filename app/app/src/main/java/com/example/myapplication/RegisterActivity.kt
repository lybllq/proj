package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.util.BackgroundTask
import com.example.myapplication.util.BackendApi
import com.example.myapplication.util.bindBackButton

class RegisterActivity : AppCompatActivity() {
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSubmitRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        bindBackButton()
        initViews()
        setupListeners()
    }

    private fun initViews() {
        etPhone = findViewById(R.id.et_register_phone)
        etPassword = findViewById(R.id.et_register_password)
        etConfirmPassword = findViewById(R.id.et_register_confirm_password)
        btnSubmitRegister = findViewById(R.id.btn_submit_register)
    }

    private fun setupListeners() {
        btnSubmitRegister.setOnClickListener { register() }
    }

    private fun register() {
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        when {
            phone.isEmpty() -> {
                toast("Please enter phone number")
                etPhone.requestFocus()
            }
            !phone.matches(Regex("^[0-9]{10,15}$")) -> {
                toast("Please enter a valid phone number")
                etPhone.requestFocus()
            }
            password.isEmpty() -> {
                toast("Please enter password")
                etPassword.requestFocus()
            }
            password.length < 6 -> {
                toast("Password must be at least 6 characters")
                etPassword.requestFocus()
            }
            confirmPassword != password -> {
                toast("Passwords do not match")
                etConfirmPassword.requestFocus()
            }
            else -> {
                btnSubmitRegister.isEnabled = false
                BackgroundTask.run(
                    task = { BackendApi.register(applicationContext, phone, password) },
                    onSuccess = {
                        btnSubmitRegister.isEnabled = true
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_LOGGED_IN, false)
                            .apply()
                        setResult(
                            RESULT_OK,
                            Intent().putExtra(EXTRA_REGISTERED_PHONE, phone)
                        )
                        toast("Registration successful")
                        finish()
                    },
                    onFailure = { error ->
                        btnSubmitRegister.isEnabled = true
                        toast("Registration failed: ${error.message}")
                    }
                )
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val EXTRA_REGISTERED_PHONE = "registered_phone"
    }
}
