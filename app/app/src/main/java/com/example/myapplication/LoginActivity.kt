package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.util.BackgroundTask
import com.example.myapplication.util.BackendApi
import com.example.myapplication.util.UserProfileManager

class LoginActivity : AppCompatActivity() {
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private val registerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val phone = result.data?.getStringExtra(EXTRA_REGISTERED_PHONE).orEmpty()
            if (phone.isNotEmpty()) {
                etPhone.setText(phone)
                etPassword.setText("")
                etPassword.requestFocus()
                toast("Registration successful, please login")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (isLoggedIn()) {
            navigateToMain()
            return
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etPhone = findViewById(R.id.et_phone)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnRegister = findViewById(R.id.btn_register)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener { attemptLogin() }
        btnRegister.setOnClickListener {
            registerLauncher.launch(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

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
            else -> {
                btnLogin.isEnabled = false
                BackgroundTask.run(
                    task = { BackendApi.login(applicationContext, phone, password) },
                    onSuccess = { user ->
                        btnLogin.isEnabled = true
                        prefs.edit()
                            .putBoolean(KEY_LOGGED_IN, true)
                            .apply()
                        UserProfileManager.getInstance(this).saveProfile(user.name, user.phone)
                        toast("Login successful")
                        navigateToMain()
                    },
                    onFailure = { error ->
                        btnLogin.isEnabled = true
                        toast("Login failed: ${error.message}")
                    }
                )
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getBoolean(KEY_LOGGED_IN, false)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
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
