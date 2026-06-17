package com.example.myapplication.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.fragment.CartFragment
import com.example.myapplication.fragment.HomeFragment
import com.example.myapplication.fragment.UserFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> CartFragment()
            2 -> UserFragment()
            else -> HomeFragment()
        }
    }

    override fun getItemCount(): Int = 3
}
