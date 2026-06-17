package com.example.myapplication.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.fragment.CurrentOrderFragment
import com.example.myapplication.fragment.HistoryOrderFragment

class OrderPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CurrentOrderFragment()
            1 -> HistoryOrderFragment()
            else -> CurrentOrderFragment()
        }
    }

    override fun getItemCount(): Int = 2
}
