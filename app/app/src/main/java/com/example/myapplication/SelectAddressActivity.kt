package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.AddressAdapter
import com.example.myapplication.model.Address
import com.example.myapplication.util.AddressManager
import com.example.myapplication.util.BackgroundTask
import com.example.myapplication.util.bindBackButton

class SelectAddressActivity : AppCompatActivity() {
    private lateinit var rvSelectAddress: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var btnAddAddress: Button
    private lateinit var addressManager: AddressManager
    private lateinit var adapter: AddressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_address)
        initViews()
        setupAdapter()
        loadAddresses()
    }

    private fun initViews() {
        rvSelectAddress = findViewById(R.id.rv_select_address)
        layoutEmpty = findViewById(R.id.layout_empty)
        btnAddAddress = findViewById(R.id.btn_add_address)

        bindBackButton()
        btnAddAddress.setOnClickListener {
            startActivityForResult(Intent(this, AddAddressActivity::class.java), REQUEST_ADD_ADDRESS)
        }
    }

    private fun setupAdapter() {
        addressManager = AddressManager.getInstance(this)
        adapter = AddressAdapter()
        adapter.setOnAddressActionListener(object : AddressAdapter.OnAddressActionListener {
            override fun onSetDefault(address: Address) = selectAddress(address)

            override fun onEdit(address: Address) = selectAddress(address)

            override fun onDelete(address: Address) = Unit
        })
        rvSelectAddress.layoutManager = LinearLayoutManager(this)
        rvSelectAddress.adapter = adapter
    }

    private fun loadAddresses() {
        BackgroundTask.run(
            task = { addressManager.getAllAddresses() },
            onSuccess = { addresses ->
                adapter.setAddressList(addresses)
                val isEmpty = addresses.isEmpty()
                layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvSelectAddress.visibility = if (isEmpty) View.GONE else View.VISIBLE
            },
            onFailure = {
                adapter.setAddressList(mutableListOf())
                layoutEmpty.visibility = View.VISIBLE
                rvSelectAddress.visibility = View.GONE
            }
        )
    }

    private fun selectAddress(address: Address) {
        val resultIntent = Intent().apply {
            putExtra("selected_address", address)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_ADD_ADDRESS) {
            loadAddresses()
        }
    }

    companion object {
        private const val REQUEST_ADD_ADDRESS = 1001
    }
}
