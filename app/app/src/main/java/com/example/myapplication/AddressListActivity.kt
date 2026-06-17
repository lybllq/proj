package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.AddressAdapter
import com.example.myapplication.model.Address
import com.example.myapplication.util.AddressManager
import com.example.myapplication.util.BackgroundTask
import com.example.myapplication.util.bindBackButton

class AddressListActivity : AppCompatActivity() {
    private lateinit var rvAddressList: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var btnAddAddress: Button

    private lateinit var addressManager: AddressManager
    private lateinit var adapter: AddressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_list)
        initViews()
        setupAdapter()
        loadAddresses()
    }

    private fun initViews() {
        rvAddressList = findViewById(R.id.rv_address_list)
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
            override fun onSetDefault(address: Address) {
                BackgroundTask.run(
                    task = { addressManager.setDefaultAddress(address.id).getOrThrow() },
                    onSuccess = {
                        loadAddresses()
                        Toast.makeText(this@AddressListActivity, "Set as default address", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(this@AddressListActivity, "Failed to set default address: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            override fun onEdit(address: Address) {
                val intent = Intent(this@AddressListActivity, AddAddressActivity::class.java)
                intent.putExtra("address", address)
                startActivityForResult(intent, REQUEST_EDIT_ADDRESS)
            }

            override fun onDelete(address: Address) {
                showDeleteConfirmDialog(address)
            }
        })
        rvAddressList.layoutManager = LinearLayoutManager(this)
        rvAddressList.adapter = adapter
    }

    private fun loadAddresses() {
        BackgroundTask.run(
            task = { addressManager.getAllAddresses() },
            onSuccess = { addresses ->
                adapter.setAddressList(addresses)
                val isEmpty = addresses.isEmpty()
                layoutEmpty.visibility = if (isEmpty) android.view.View.VISIBLE else android.view.View.GONE
                rvAddressList.visibility = if (isEmpty) android.view.View.GONE else android.view.View.VISIBLE
            },
            onFailure = { error ->
                adapter.setAddressList(mutableListOf())
                layoutEmpty.visibility = android.view.View.VISIBLE
                rvAddressList.visibility = android.view.View.GONE
                Toast.makeText(this, "Failed to load addresses: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showDeleteConfirmDialog(address: Address) {
        AlertDialog.Builder(this)
            .setTitle("Delete Address")
            .setMessage("Are you sure you want to delete this address?")
            .setPositiveButton("Delete") { _, _ ->
                BackgroundTask.run(
                    task = { addressManager.deleteAddress(address.id).getOrThrow() },
                    onSuccess = {
                        loadAddresses()
                        Toast.makeText(this, "Address deleted", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(this, "Failed to delete address: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && (requestCode == REQUEST_ADD_ADDRESS || requestCode == REQUEST_EDIT_ADDRESS)) {
            loadAddresses()
        }
    }

    companion object {
        private const val REQUEST_ADD_ADDRESS = 1001
        private const val REQUEST_EDIT_ADDRESS = 1002
    }
}
