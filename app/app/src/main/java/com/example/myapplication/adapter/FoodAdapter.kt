package com.example.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Food
import com.example.myapplication.util.CartManager

class FoodAdapter(
    private val context: Context,
    private val foods: List<Food>
) : RecyclerView.Adapter<FoodAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val food = foods[position]
        holder.tvName.text = food.name
        holder.tvDescription.text = food.description
        holder.tvPrice.text = String.format("$%.2f", food.price)
        holder.tvMonthlySales.text = "${food.monthlySales} sold"
        holder.btnAddToCart.setOnClickListener {
            CartManager.getInstance().addToCart(food)
            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = foods.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_food_name)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_food_description)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_food_price)
        val tvMonthlySales: TextView = itemView.findViewById(R.id.tv_monthly_sales)
        val btnAddToCart: Button = itemView.findViewById(R.id.btn_add_to_cart)
    }
}
