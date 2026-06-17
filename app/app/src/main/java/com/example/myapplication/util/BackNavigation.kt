package com.example.myapplication.util

import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

fun AppCompatActivity.bindBackButton(@IdRes backButtonId: Int = R.id.btn_back) {
    findViewById<View?>(backButtonId)?.setOnClickListener {
        onBackPressedDispatcher.onBackPressed()
    }
}
