package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Address

class AddressSelectAdapter : RecyclerView.Adapter<AddressSelectAdapter.ViewHolder>() {
    private var addressList: List<Address> = emptyList()
    private var selectedAddressId: String? = null
    private var listener: OnAddressSelectListener? = null

    interface OnAddressSelectListener {
        fun onAddressSelected(address: Address)
    }

    fun setOnAddressSelectListener(listener: OnAddressSelectListener) {
        this.listener = listener
    }

    fun setAddressList(addressList: List<Address>?) {
        this.addressList = addressList ?: emptyList()
        notifyDataSetChanged()
    }

    fun setSelectedAddressId(addressId: String?) {
        selectedAddressId = addressId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = addressList[position]
        holder.bind(address, address.id == selectedAddressId)
    }

    override fun getItemCount(): Int = addressList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rootView: View = itemView
        private val tvLabel: TextView = itemView.findViewById(R.id.tv_address_label)
        private val tvDefaultTag: TextView = itemView.findViewById(R.id.tv_default_tag)
        private val tvName: TextView = itemView.findViewById(R.id.tv_address_name)
        private val tvPhone: TextView = itemView.findViewById(R.id.tv_address_phone)
        private val tvAddress: TextView = itemView.findViewById(R.id.tv_address_detail)
        private val selectedIndicator: View? = itemView.findViewById(R.id.view_selected_indicator)

        fun bind(address: Address, isSelected: Boolean) {
            tvLabel.text = address.label
            tvDefaultTag.visibility = if (address.isDefault) View.VISIBLE else View.GONE
            tvName.text = address.name
            tvPhone.text = address.phone
            tvAddress.text = address.getFullAddress()
            selectedIndicator?.visibility = if (isSelected) View.VISIBLE else View.GONE
            rootView.setOnClickListener { listener?.onAddressSelected(address) }
        }
    }
}
