package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.AddressSelectAdapter
import com.example.myapplication.adapter.CheckoutAdapter
import com.example.myapplication.model.Address
import com.example.myapplication.model.CartItem
import com.example.myapplication.model.Order
import com.example.myapplication.util.AddressManager
import com.example.myapplication.util.BackgroundTask
import com.example.myapplication.util.CartManager
import com.example.myapplication.util.OrderManager
import com.example.myapplication.util.bindBackButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Date
import kotlin.random.Random

class CheckoutActivity : AppCompatActivity() {
    private lateinit var addressManager: AddressManager
    private var selectedAddress: Address? = null
    private lateinit var selectedItems: List<CartItem>
    private var addressDialog: BottomSheetDialog? = null

    private lateinit var layoutAddressInfo: LinearLayout
    private lateinit var layoutNoAddress: LinearLayout
    private lateinit var layoutHasAddress: LinearLayout
    private lateinit var btnChangeAddress: TextView
    private lateinit var tvAddressLabel: TextView
    private lateinit var tvReceiverName: TextView
    private lateinit var tvReceiverPhone: TextView
    private lateinit var tvReceiverAddress: TextView
    private lateinit var recyclerViewCheckout: RecyclerView
    private lateinit var tvTotalAmount: TextView
    private lateinit var btnSubmitOrder: Button
    private lateinit var checkoutAdapter: CheckoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        addressManager = AddressManager.getInstance(this)
        initViews()
        loadDefaultAddress()
        loadData()
        setupListeners()
    }

    private fun initViews() {
        bindBackButton()
        layoutAddressInfo = findViewById(R.id.layout_address_info)
        layoutNoAddress = findViewById(R.id.layout_no_address)
        layoutHasAddress = findViewById(R.id.layout_has_address)
        btnChangeAddress = findViewById(R.id.btn_change_address)
        tvAddressLabel = findViewById(R.id.tv_address_label)
        tvReceiverName = findViewById(R.id.tv_receiver_name)
        tvReceiverPhone = findViewById(R.id.tv_receiver_phone)
        tvReceiverAddress = findViewById(R.id.tv_receiver_address)
        recyclerViewCheckout = findViewById(R.id.recycler_view_checkout)
        tvTotalAmount = findViewById(R.id.tv_total_amount)
        btnSubmitOrder = findViewById(R.id.btn_submit_order)
    }

    private fun loadDefaultAddress() {
        BackgroundTask.run(
            task = { addressManager.getDefaultAddress() },
            onSuccess = { address ->
                selectedAddress = address
                updateAddressDisplay()
            },
            onFailure = {
                selectedAddress = null
                updateAddressDisplay()
                toast("Failed to load delivery address")
            }
        )
    }

    private fun updateAddressDisplay() {
        val address = selectedAddress
        if (address == null) {
            layoutNoAddress.visibility = View.VISIBLE
            layoutHasAddress.visibility = View.GONE
            return
        }
        layoutNoAddress.visibility = View.GONE
        layoutHasAddress.visibility = View.VISIBLE
        tvAddressLabel.text = address.label
        tvReceiverName.text = address.name
        tvReceiverPhone.text = address.phone
        tvReceiverAddress.text = address.getFullAddress()
    }

    private fun loadData() {
        selectedItems = CartManager.getInstance().getCartItems()
        recyclerViewCheckout.layoutManager = LinearLayoutManager(this)
        checkoutAdapter = CheckoutAdapter(this, selectedItems)
        recyclerViewCheckout.adapter = checkoutAdapter

        val totalPrice = CartManager.getInstance().getTotalPrice()
        val finalAmount = totalPrice + 5.0f
        tvTotalAmount.text = String.format("$%.2f", totalPrice)
        findViewById<TextView>(R.id.tv_final_amount).text = String.format("$%.2f", finalAmount)
    }

    private fun setupListeners() {
        val addressClickListener = View.OnClickListener { showAddressSelectDialog() }
        btnChangeAddress.setOnClickListener(addressClickListener)
        layoutAddressInfo.setOnClickListener(addressClickListener)
        btnSubmitOrder.setOnClickListener { submitOrder() }
    }

    private fun showAddressSelectDialog() {
        val dialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_address, null as ViewGroup?)
        dialog.setContentView(dialogView)
        addressDialog = dialog

        val rvDialogAddress = dialogView.findViewById<RecyclerView>(R.id.rv_dialog_address)
        val layoutDialogEmpty = dialogView.findViewById<LinearLayout>(R.id.layout_dialog_empty)
        val btnCloseDialog = dialogView.findViewById<ImageView>(R.id.btn_close_dialog)
        val btnManageAddress = dialogView.findViewById<Button>(R.id.btn_manage_address)
        val btnAddNewAddress = dialogView.findViewById<Button>(R.id.btn_add_new_address)

        rvDialogAddress.visibility = View.GONE
        layoutDialogEmpty.visibility = View.VISIBLE
        BackgroundTask.run(
            task = { addressManager.getAllAddresses() },
            onSuccess = { addresses ->
                if (addresses.isEmpty()) {
                    rvDialogAddress.visibility = View.GONE
                    layoutDialogEmpty.visibility = View.VISIBLE
                } else {
                    rvDialogAddress.visibility = View.VISIBLE
                    layoutDialogEmpty.visibility = View.GONE
                    val adapter = AddressSelectAdapter()
                    adapter.setAddressList(addresses)
                    adapter.setSelectedAddressId(selectedAddress?.id)
                    adapter.setOnAddressSelectListener(object : AddressSelectAdapter.OnAddressSelectListener {
                        override fun onAddressSelected(address: Address) {
                            selectedAddress = address
                            updateAddressDisplay()
                            addressDialog?.dismiss()
                        }
                    })
                    rvDialogAddress.layoutManager = LinearLayoutManager(this)
                    rvDialogAddress.adapter = adapter
                }
            },
            onFailure = { toast("Failed to load addresses") }
        )

        btnCloseDialog.setOnClickListener { addressDialog?.dismiss() }
        btnManageAddress.setOnClickListener {
            addressDialog?.dismiss()
            startActivityForResult(Intent(this, AddressListActivity::class.java), REQUEST_MANAGE_ADDRESS)
        }
        btnAddNewAddress.setOnClickListener {
            addressDialog?.dismiss()
            startActivityForResult(Intent(this, AddAddressActivity::class.java), REQUEST_ADD_ADDRESS)
        }
        dialog.show()
    }

    private fun submitOrder() {
        val address = selectedAddress
        if (address == null) {
            toast("Please select a delivery address first")
            return
        }
        if (selectedItems.isEmpty()) {
            toast("Cart is empty")
            return
        }

        val totalAmount = CartManager.getInstance().getTotalPrice()
        val finalAmount = totalAmount + 5.0f
        val orderItems = selectedItems.map { CartItem(it.food, it.quantity) }
        val statuses = listOf(Order.STATUS_PAID, Order.STATUS_PREPARING, Order.STATUS_DELIVERING)
        val orderStatus = statuses[Random.nextInt(statuses.size)]

        val order = Order(
            id = 0,
            items = orderItems,
            totalAmount = finalAmount,
            status = orderStatus,
            orderTime = Date(),
            address = address.getFullAddress(),
            userName = address.name,
            userPhone = address.phone
        )
        btnSubmitOrder.isEnabled = false
        btnSubmitOrder.text = "Submitting..."
        BackgroundTask.run(
            task = { OrderManager.getInstance().addOrder(applicationContext, order).getOrThrow() },
            onSuccess = {
                btnSubmitOrder.isEnabled = true
                btnSubmitOrder.text = "Place Order"
                Toast.makeText(
                    this,
                    "Order submitted successfully!\nReceiver: ${address.name}\nPhone: ${address.phone}\nAddress: ${address.getFullAddress()}\nTotal: $${String.format("%.2f", finalAmount)}",
                    Toast.LENGTH_LONG
                ).show()

                CartManager.getInstance().clearCart()
                Handler(Looper.getMainLooper()).postDelayed({ finish() }, 2000L)
            },
            onFailure = { error ->
                btnSubmitOrder.isEnabled = true
                btnSubmitOrder.text = "Place Order"
                toast("Order submission failed: ${error.message ?: "network or server error"}")
            }
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && (requestCode == REQUEST_ADD_ADDRESS || requestCode == REQUEST_MANAGE_ADDRESS)) {
            loadDefaultAddress()
        }
    }

    override fun onResume() {
        super.onResume()
        BackgroundTask.run(
            task = { addressManager.getDefaultAddress() },
            onSuccess = { currentDefaultAddress ->
                if (currentDefaultAddress != null && selectedAddress?.id != currentDefaultAddress.id) {
                    selectedAddress = currentDefaultAddress
                    updateAddressDisplay()
                }
            },
            onFailure = { }
        )
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_ADD_ADDRESS = 1001
        private const val REQUEST_MANAGE_ADDRESS = 1002
    }
}
