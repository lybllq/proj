package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.util.bindBackButton

class CustomerServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_service)
        bindBackButton()
        setupActions()
    }

    private fun setupActions() {
        findViewById<Button>(R.id.btn_call_hotline).setOnClickListener {
            toast("Calling hotline: 400-800-1234")
        }
        findViewById<Button>(R.id.btn_online_chat).setOnClickListener {
            toast("Connecting to online customer service...")
        }
        findViewById<Button>(R.id.btn_feedback).setOnClickListener {
            toast("Feedback submitted, thank you")
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
