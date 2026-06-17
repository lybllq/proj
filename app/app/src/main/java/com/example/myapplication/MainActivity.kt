package com.example.myapplication

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.adapter.ViewPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViews()
        setupViewPager()
        setupBottomNavigation()
        setupBackNavigation()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.view_pager)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
    }

    private fun setupViewPager() {
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        viewPager.isUserInputEnabled = true
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> bottomNavigationView.selectedItemId = R.id.nav_home
                    1 -> bottomNavigationView.selectedItemId = R.id.nav_cart
                    2 -> bottomNavigationView.selectedItemId = R.id.nav_user
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    viewPager.setCurrentItem(0, true)
                    true
                }
                R.id.nav_cart -> {
                    viewPager.setCurrentItem(1, true)
                    true
                }
                R.id.nav_user -> {
                    viewPager.setCurrentItem(2, true)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewPager.currentItem != 0) {
                    viewPager.setCurrentItem(0, true)
                } else {
                    finish()
                }
            }
        })
    }
}
