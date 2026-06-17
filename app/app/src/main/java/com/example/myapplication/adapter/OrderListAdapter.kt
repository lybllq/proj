package com.example.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Order
import java.text.SimpleDateFormat
import java.util.Locale

class OrderListAdapter(
    private val context: Context,
    private val orders: List<Order>,
    private val listener: OnOrderClickListener? = null
) : RecyclerView.Adapter<OrderListAdapter.ViewHolder>() {

    interface OnOrderClickListener {
        fun onOrderClick(order: Order)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        holder.tvOrderId?.text = String.format("%010d", order.id)
        holder.tvOrderTime?.text = dateFormat.format(order.orderTime)
        holder.tvOrderStatus?.text = order.status
        holder.tvOrderAddress?.text = order.address
        holder.tvOrderTotal?.text = String.format("$%.2f", order.totalAmount)

        if (order.items.isNotEmpty()) {
            holder.recyclerViewOrderItems?.layoutManager = LinearLayoutManager(context)
            holder.recyclerViewOrderItems?.adapter = OrderItemAdapter(context, order.items)
        }
        holder.itemView.setOnClickListener { listener?.onOrderClick(order) }
    }

    override fun getItemCount(): Int = orders.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView? = itemView.findViewById(R.id.tv_order_id)
        val tvOrderTime: TextView? = itemView.findViewById(R.id.tv_order_time)
        val tvOrderStatus: TextView? = itemView.findViewById(R.id.tv_order_status)
        val tvOrderAddress: TextView? = itemView.findViewById(R.id.tv_order_address)
        val tvOrderTotal: TextView? = itemView.findViewById(R.id.tv_order_total)
        val recyclerViewOrderItems: RecyclerView? = itemView.findViewById(R.id.recycler_view_order_items)
    }
}
