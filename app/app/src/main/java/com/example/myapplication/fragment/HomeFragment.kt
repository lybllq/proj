package com.example.myapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.RestaurantAdapter
import com.example.myapplication.model.Food
import com.example.myapplication.model.Restaurant
import com.example.myapplication.util.BackgroundTask
import com.example.myapplication.util.BackendApi

class HomeFragment : Fragment() {
    private lateinit var recyclerViewRestaurants: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter
    private val restaurantList = mutableListOf<Restaurant>()
    private lateinit var tvAddress: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(view)
        initData()
        return view
    }

    private fun initViews(view: View) {
        tvAddress = view.findViewById(R.id.tv_address)
        recyclerViewRestaurants = view.findViewById(R.id.recycler_view_restaurants)
        recyclerViewRestaurants.layoutManager = LinearLayoutManager(context)
        restaurantAdapter = RestaurantAdapter(requireContext(), restaurantList)
        recyclerViewRestaurants.adapter = restaurantAdapter
    }

    private fun initData() {
        restaurantList.clear()
        restaurantAdapter.notifyDataSetChanged()
        val appContext = requireContext().applicationContext
        BackgroundTask.run(
            task = { BackendApi.getFoods(appContext) },
            onSuccess = { backendFoods -> showRestaurants(backendFoods) },
            onFailure = { showRestaurants(mutableListOf()) }
        )
    }

    private fun showRestaurants(backendFoods: MutableList<Food>) {
        restaurantList.clear()
        if (backendFoods.isNotEmpty()) {
            val sharedRestaurant = Restaurant(
                id = 1,
                name = "DeliFood Delivery",
                description = "Shared backend menu",
                rating = 4.8f,
                deliveryTime = 30,
                deliveryFee = 5.0f,
                minOrder = 20.0f,
                imageUrl = ""
            )
            sharedRestaurant.foods.addAll(backendFoods)
            restaurantList.add(sharedRestaurant)
        } else {
            val fallbackRestaurant = Restaurant(1, "DeliFood", "Fallback menu", 4.6f, 30, 5.0f, 20.0f, "")
            fallbackRestaurant.foods.add(Food(1, "Beef Bowl", "Default", 32.0f, "", 1, 0))
            fallbackRestaurant.foods.add(Food(2, "Chicken Burger", "Default", 25.0f, "", 1, 0))
            restaurantList.add(fallbackRestaurant)
        }
        restaurantAdapter.notifyDataSetChanged()
    }
}
