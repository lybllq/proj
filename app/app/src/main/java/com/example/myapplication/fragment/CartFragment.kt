package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.CheckoutActivity
import com.example.myapplication.R
import com.example.myapplication.adapter.CartAdapter
import com.example.myapplication.util.CartManager

class CartFragment : Fragment() {
    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnCheckout: Button
    private lateinit var tvEmptyCart: TextView
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)
        initViews(view)
        updateUI()
        return view
    }

    private fun initViews(view: View) {
        recyclerViewCart = view.findViewById(R.id.recycler_view_cart)
        tvTotalPrice = view.findViewById(R.id.tv_total_price)
        btnCheckout = view.findViewById(R.id.btn_checkout)
        tvEmptyCart = view.findViewById(R.id.tv_empty_cart)

        recyclerViewCart.layoutManager = LinearLayoutManager(context)
        cartAdapter = CartAdapter(
            requireContext(),
            CartManager.getInstance().getCartItems(),
            object : CartAdapter.OnCartChangeListener {
                override fun onCartChanged() {
                    updateUI()
                }
            }
        )
        recyclerViewCart.adapter = cartAdapter

        btnCheckout.setOnClickListener {
            if (CartManager.getInstance().getCartItems().isEmpty()) {
                Toast.makeText(context, "Cart is empty", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(context, CheckoutActivity::class.java))
            }
        }
    }

    private fun updateUI() {
        val totalPrice = CartManager.getInstance().getTotalPrice()
        tvTotalPrice.text = String.format("$%.2f", totalPrice)
        val isEmpty = CartManager.getInstance().getCartItems().isEmpty()
        tvEmptyCart.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerViewCart.visibility = if (isEmpty) View.GONE else View.VISIBLE
        cartAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}
