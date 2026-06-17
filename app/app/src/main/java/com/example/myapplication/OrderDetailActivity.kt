package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.OrderItemAdapter
import com.example.myapplication.model.Order
import com.example.myapplication.util.BackgroundTask
import com.example.myapplication.util.OrderManager
import com.example.myapplication.util.bindBackButton
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private lateinit var tvOrderId: TextView
    private lateinit var tvOrderStatus: TextView
    private lateinit var tvOrderTime: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var recyclerViewItems: RecyclerView
    private lateinit var btnAction: Button

    private var order: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        initViews()
        loadOrderDetail()
    }

    private fun initViews() {
        bindBackButton()
        tvOrderId = findViewById(R.id.tv_order_id)
        tvOrderStatus = findViewById(R.id.tv_order_status)
        tvOrderTime = findViewById(R.id.tv_order_time)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserPhone = findViewById(R.id.tv_user_phone)
        tvAddress = findViewById(R.id.tv_address)
        tvTotalAmount = findViewById(R.id.tv_total_amount)
        recyclerViewItems = findViewById(R.id.recycler_view_items)
        btnAction = findViewById(R.id.btn_action)
        recyclerViewItems.layoutManager = LinearLayoutManager(this)
    }

    private fun loadOrderDetail() {
        val orderId = intent.getIntExtra("order_id", -1)
        if (orderId == -1) {
            toast("Order not found")
            finish()
            return
        }
        BackgroundTask.run(
            task = { OrderManager.getInstance().getOrderById(applicationContext, orderId) },
            onSuccess = { targetOrder ->
                if (targetOrder == null) {
                    toast("Order not found")
                    finish()
                } else {
                    order = targetOrder
                    tvOrderId.text = "Order ID: ${String.format("%010d", targetOrder.id)}"
                    tvOrderStatus.text = targetOrder.status
                    tvOrderTime.text = "Order Time: ${dateFormat.format(targetOrder.orderTime)}"
                    tvUserName.text = targetOrder.userName ?: "User"
                    tvUserPhone.text = targetOrder.userPhone ?: "138****8888"
                    tvAddress.text = targetOrder.address
                    tvTotalAmount.text = String.format("$%.2f", targetOrder.totalAmount)
                    findViewById<TextView?>(R.id.tv_final_amount)?.text = String.format("$%.2f", targetOrder.totalAmount)
                    recyclerViewItems.adapter = OrderItemAdapter(this, targetOrder.items)
                    setupActionButton()
                }
            },
            onFailure = {
                toast("Failed to load order")
                finish()
            }
        )
    }

    private fun setupActionButton() {
        val currentOrder = order ?: return
        when (currentOrder.status) {
            Order.STATUS_PENDING -> {
                btnAction.text = "Pay Now"
                btnAction.visibility = android.view.View.VISIBLE
                btnAction.setOnClickListener {
                    toast("Redirecting to payment...")
                    currentOrder.status = Order.STATUS_PAID
                    loadOrderDetail()
                }
            }
            Order.STATUS_PAID, Order.STATUS_PREPARING, Order.STATUS_DELIVERING -> {
                btnAction.text = "Contact Driver"
                btnAction.visibility = android.view.View.VISIBLE
                btnAction.setOnClickListener { toast("Calling driver...") }
            }
            Order.STATUS_COMPLETED -> {
                btnAction.text = "Reorder"
                btnAction.visibility = android.view.View.VISIBLE
                btnAction.setOnClickListener { toast("Added to cart") }
            }
            else -> btnAction.visibility = android.view.View.GONE
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
