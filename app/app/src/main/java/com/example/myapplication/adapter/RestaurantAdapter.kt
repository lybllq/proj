package com.example.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Restaurant

class RestaurantAdapter(
    private val context: Context,
    private val restaurants: List<Restaurant>
) : RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.tvName.text = restaurant.name
        holder.tvDescription.text = restaurant.description
        holder.tvRating.text = String.format("%.1f", restaurant.rating)
        holder.tvDeliveryTime.text = "${restaurant.deliveryTime} min"
        holder.tvDeliveryFee.text = "Delivery $${restaurant.deliveryFee}"
        holder.tvMinOrder.text = "Min $${restaurant.minOrder}"
        holder.recyclerViewFoods.layoutManager = LinearLayoutManager(context)
        holder.recyclerViewFoods.adapter = FoodAdapter(context, restaurant.foods)
    }

    override fun getItemCount(): Int = restaurants.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_restaurant_name)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_restaurant_description)
        val tvRating: TextView = itemView.findViewById(R.id.tv_restaurant_rating)
        val tvDeliveryTime: TextView = itemView.findViewById(R.id.tv_delivery_time)
        val tvDeliveryFee: TextView = itemView.findViewById(R.id.tv_delivery_fee)
        val tvMinOrder: TextView = itemView.findViewById(R.id.tv_min_order)
        val recyclerViewFoods: RecyclerView = itemView.findViewById(R.id.recycler_view_foods)
    }
}
