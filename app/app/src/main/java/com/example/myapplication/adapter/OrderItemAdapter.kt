package com.example.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.CartItem

class OrderItemAdapter(
    private val context: Context,
    private val items: List<CartItem>
) : RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_order_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.food.name
        holder.tvQuantity.text = "x${item.quantity}"
        holder.tvPrice.text = String.format("¥%.2f", item.getTotalPrice())
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_order_item_name)
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_order_item_quantity)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_order_item_price)
    }
}
