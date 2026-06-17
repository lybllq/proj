package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.adapter.OrderPagerAdapter
import com.example.myapplication.util.bindBackButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OrderListActivity : AppCompatActivity() {
    private var viewPager: ViewPager2? = null
    private var tabLayout: TabLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            setContentView(R.layout.activity_order_list)
            initViews()
            setupViewPager()
        }.onFailure {
            finish()
        }
    }

    private fun initViews() {
        bindBackButton()
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
    }

    private fun setupViewPager() {
        val vp = viewPager ?: return
        val tabs = tabLayout ?: return
        vp.adapter = OrderPagerAdapter(this)
        TabLayoutMediator(tabs, vp) { tab, position ->
            when (position) {
                0 -> tab.text = "Current Orders"
                1 -> tab.text = "History"
            }
        }.attach()
    }
}
