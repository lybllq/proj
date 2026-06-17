package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.OrderDetailActivity
import com.example.myapplication.R
import com.example.myapplication.adapter.OrderListAdapter
import com.example.myapplication.model.Order
import com.example.myapplication.util.BackgroundTask
import com.example.myapplication.util.OrderManager

class HistoryOrderFragment : Fragment() {
    private lateinit var recyclerViewOrders: RecyclerView
    private lateinit var tvEmptyOrders: TextView
    private var orderListAdapter: OrderListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_order_list, container, false)
        recyclerViewOrders = view.findViewById(R.id.recycler_view_orders)
        tvEmptyOrders = view.findViewById(R.id.tv_empty_orders)
        recyclerViewOrders.layoutManager = LinearLayoutManager(context)
        loadOrders()
        return view
    }

    private fun loadOrders() {
        val appContext = requireContext().applicationContext
        BackgroundTask.run(
            task = { OrderManager.getInstance().getOrders(appContext).filter { it.isHistoryOrder() } },
            onSuccess = { historyOrders -> showOrders(historyOrders) },
            onFailure = { showOrders(emptyList()) }
        )
    }

    private fun showOrders(historyOrders: List<Order>) {
        if (historyOrders.isEmpty()) {
            tvEmptyOrders.visibility = View.VISIBLE
            tvEmptyOrders.text = "No order history"
            recyclerViewOrders.visibility = View.GONE
            return
        }
        tvEmptyOrders.visibility = View.GONE
        recyclerViewOrders.visibility = View.VISIBLE
        orderListAdapter = OrderListAdapter(
            requireContext(),
            historyOrders,
            object : OrderListAdapter.OnOrderClickListener {
                override fun onOrderClick(order: Order) {
                    val intent = Intent(activity, OrderDetailActivity::class.java)
                    intent.putExtra("order_id", order.id)
                    startActivity(intent)
                }
            }
        )
        recyclerViewOrders.adapter = orderListAdapter
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }
}
