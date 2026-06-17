package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Address

class AddressAdapter : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {
    private var addressList: List<Address> = emptyList()
    private var listener: OnAddressActionListener? = null

    interface OnAddressActionListener {
        fun onSetDefault(address: Address)
        fun onEdit(address: Address)
        fun onDelete(address: Address)
    }

    fun setOnAddressActionListener(listener: OnAddressActionListener) {
        this.listener = listener
    }

    fun setAddressList(addressList: List<Address>?) {
        this.addressList = addressList ?: emptyList()
        notifyDataSetChanged()
    }

    fun getAddressList(): List<Address> = addressList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(addressList[position])
    }

    override fun getItemCount(): Int = addressList.size

    inner class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLabel: TextView = itemView.findViewById(R.id.tv_address_label)
        private val tvDefaultTag: TextView = itemView.findViewById(R.id.tv_default_tag)
        private val tvName: TextView = itemView.findViewById(R.id.tv_address_name)
        private val tvPhone: TextView = itemView.findViewById(R.id.tv_address_phone)
        private val tvDetail: TextView = itemView.findViewById(R.id.tv_address_detail)
        private val btnSetDefault: TextView = itemView.findViewById(R.id.btn_set_default)
        private val btnEdit: TextView = itemView.findViewById(R.id.btn_edit_address)
        private val btnDelete: TextView = itemView.findViewById(R.id.btn_delete_address)

        fun bind(address: Address) {
            tvLabel.text = address.label
            tvName.text = address.name
            tvPhone.text = address.phone
            tvDetail.text = address.getFullAddress()

            if (address.isDefault) {
                tvDefaultTag.visibility = View.VISIBLE
                btnSetDefault.visibility = View.GONE
            } else {
                tvDefaultTag.visibility = View.GONE
                btnSetDefault.visibility = View.VISIBLE
            }

            btnSetDefault.setOnClickListener { listener?.onSetDefault(address) }
            btnEdit.setOnClickListener { listener?.onEdit(address) }
            btnDelete.setOnClickListener { listener?.onDelete(address) }
        }
    }
}
