package com.example.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.CartItem

class CheckoutAdapter(
    private val context: Context,
    private val cartItems: List<CartItem>
) : RecyclerView.Adapter<CheckoutAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = cartItems[position]
        holder.tvName.text = item.food.name
        holder.tvPrice.text = String.format("¥%.2f", item.food.price)
        holder.tvQuantity.text = "x${item.quantity}"
        holder.tvSubtotal.text = String.format("¥%.2f", item.getTotalPrice())
    }

    override fun getItemCount(): Int = cartItems.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_checkout_food_name)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_checkout_food_price)
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_checkout_quantity)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tv_checkout_subtotal)
    }
}
