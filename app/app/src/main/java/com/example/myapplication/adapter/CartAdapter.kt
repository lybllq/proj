package com.example.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.CartItem
import com.example.myapplication.util.CartManager

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<CartItem>,
    private val listener: OnCartChangeListener?
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    interface OnCartChangeListener {
        fun onCartChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = cartItems[position]
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.selected
        holder.tvName.text = item.food.name
        holder.tvPrice.text = String.format("¥%.2f", item.food.price)
        holder.tvQuantity.text = item.quantity.toString()
        holder.tvTotalPrice.text = String.format("¥%.2f", item.getTotalPrice())

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.selected = isChecked
            listener?.onCartChanged()
        }

        holder.btnMinus.setOnClickListener {
            val newQuantity = item.quantity - 1
            if (newQuantity <= 0) {
                cartItems.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, cartItems.size)
            } else {
                CartManager.getInstance().updateQuantity(item.food, newQuantity)
                notifyItemChanged(position)
            }
            listener?.onCartChanged()
        }

        holder.btnPlus.setOnClickListener {
            CartManager.getInstance().updateQuantity(item.food, item.quantity + 1)
            notifyItemChanged(position)
            listener?.onCartChanged()
        }
    }

    override fun getItemCount(): Int = cartItems.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_select)
        val tvName: TextView = itemView.findViewById(R.id.tv_cart_food_name)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_cart_food_price)
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_quantity)
        val tvTotalPrice: TextView = itemView.findViewById(R.id.tv_cart_item_total)
        val btnMinus: Button = itemView.findViewById(R.id.btn_minus)
        val btnPlus: Button = itemView.findViewById(R.id.btn_plus)
    }
}
