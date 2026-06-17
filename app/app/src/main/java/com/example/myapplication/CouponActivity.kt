package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.util.bindBackButton

class CouponActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupon)
        bindBackButton()
        setupCouponList()
    }

    private fun setupCouponList() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_coupon)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CouponAdapter(
            listOf(
                CouponItem("New User Coupon", "Save $5 on orders over $20", "Valid until 2026-12-31"),
                CouponItem("Delivery Coupon", "Free delivery for one order", "Valid until 2026-10-31"),
                CouponItem("Dinner Coupon", "Save $8 on dinner orders over $35", "Valid until 2026-09-30")
            )
        )
    }
}

data class CouponItem(
    val title: String,
    val description: String,
    val validity: String
)

class CouponAdapter(
    private val items: List<CouponItem>
) : RecyclerView.Adapter<CouponAdapter.CouponViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coupon, parent, false)
        return CouponViewHolder(view)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description
        holder.tvValidity.text = item.validity
    }

    override fun getItemCount(): Int = items.size

    class CouponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_coupon_title)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_coupon_description)
        val tvValidity: TextView = itemView.findViewById(R.id.tv_coupon_validity)
    }
}
