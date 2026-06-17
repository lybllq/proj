package com.example.myapplication

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.util.UserProfileManager
import com.example.myapplication.util.bindBackButton

class EditProfileActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var etUserName: EditText
    private lateinit var etUserPhone: EditText
    private lateinit var btnSave: Button
    private lateinit var userProfileManager: UserProfileManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        initViews()
        fillCurrentProfile()
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tv_title)
        etUserName = findViewById(R.id.et_user_name)
        etUserPhone = findViewById(R.id.et_user_phone)
        btnSave = findViewById(R.id.btn_save)
        userProfileManager = UserProfileManager.getInstance(this)

        tvTitle.text = "Edit Profile"
        bindBackButton()
        btnSave.setOnClickListener { saveProfile() }
    }

    private fun fillCurrentProfile() {
        etUserName.setText(userProfileManager.getUserName())
        etUserPhone.setText(userProfileManager.getUserPhone())
    }

    private fun saveProfile() {
        val userName = etUserName.text.toString().trim()
        val userPhone = etUserPhone.text.toString().trim()

        when {
            TextUtils.isEmpty(userName) -> {
                toast("Please enter user name")
                etUserName.requestFocus()
                return
            }

            TextUtils.isEmpty(userPhone) -> {
                toast("Please enter phone number")
                etUserPhone.requestFocus()
                return
            }

            !isValidPhone(userPhone) -> {
                toast("Please enter a valid phone number")
                etUserPhone.requestFocus()
                return
            }
        }

        userProfileManager.saveProfile(userName, userPhone)
        toast("Profile updated")
        setResult(RESULT_OK)
        finish()
    }

    private fun isValidPhone(phone: String): Boolean = phone.matches(Regex("^[0-9]{10,15}$"))

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
