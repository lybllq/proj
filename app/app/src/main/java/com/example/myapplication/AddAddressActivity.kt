package com.example.myapplication

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.myapplication.model.Address
import com.example.myapplication.util.AddressManager
import com.example.myapplication.util.BackgroundTask
import com.example.myapplication.util.BackendConfig
import com.example.myapplication.util.bindBackButton

class AddAddressActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etProvince: EditText
    private lateinit var etCity: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etDetailAddress: EditText
    private lateinit var rgLabel: RadioGroup
    private lateinit var rbHome: RadioButton
    private lateinit var rbCompany: RadioButton
    private lateinit var rbSchool: RadioButton
    private lateinit var switchDefault: SwitchCompat
    private lateinit var btnSave: Button

    private lateinit var addressManager: AddressManager
    private var editingAddress: Address? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_address)
        initViews()
        checkEditMode()
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tv_title)
        etName = findViewById(R.id.et_name)
        etPhone = findViewById(R.id.et_phone)
        etProvince = findViewById(R.id.et_province)
        etCity = findViewById(R.id.et_city)
        etDistrict = findViewById(R.id.et_district)
        etDetailAddress = findViewById(R.id.et_detail_address)
        rgLabel = findViewById(R.id.rg_label)
        rbHome = findViewById(R.id.rb_home)
        rbCompany = findViewById(R.id.rb_company)
        rbSchool = findViewById(R.id.rb_school)
        switchDefault = findViewById(R.id.switch_default)
        btnSave = findViewById(R.id.btn_save)

        addressManager = AddressManager.getInstance(this)
        bindBackButton()
        btnSave.setOnClickListener { saveAddress() }
    }

    @Suppress("DEPRECATION")
    private fun checkEditMode() {
        editingAddress = intent.getSerializableExtra("address") as? Address
        if (editingAddress != null) {
            isEditMode = true
            tvTitle.text = "Edit Address"
            fillAddressData(editingAddress!!)
        } else {
            isEditMode = false
            tvTitle.text = "Add Address"
            rbHome.isChecked = true
        }
    }

    private fun fillAddressData(address: Address) {
        etName.setText(address.name)
        etPhone.setText(address.phone)
        etProvince.setText(address.province)
        etCity.setText(address.city)
        etDistrict.setText(address.district)
        etDetailAddress.setText(address.detailAddress)
        switchDefault.isChecked = address.isDefault
        when (address.label) {
            "Home" -> rbHome.isChecked = true
            "Company" -> rbCompany.isChecked = true
            "School" -> rbSchool.isChecked = true
            else -> rbHome.isChecked = true
        }
    }

    private fun saveAddress() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val province = etProvince.text.toString().trim()
        val city = etCity.text.toString().trim()
        val district = etDistrict.text.toString().trim()
        val detailAddress = etDetailAddress.text.toString().trim()

        when {
            TextUtils.isEmpty(name) -> {
                toast("Please enter receiver name")
                etName.requestFocus()
                return
            }
            TextUtils.isEmpty(phone) -> {
                toast("Please enter phone number")
                etPhone.requestFocus()
                return
            }
            !isValidPhone(phone) -> {
                toast("Please enter a valid phone number")
                etPhone.requestFocus()
                return
            }
            TextUtils.isEmpty(province) -> {
                toast("Please enter province/state")
                etProvince.requestFocus()
                return
            }
            TextUtils.isEmpty(city) -> {
                toast("Please enter city")
                etCity.requestFocus()
                return
            }
            TextUtils.isEmpty(district) -> {
                toast("Please enter district")
                etDistrict.requestFocus()
                return
            }
            TextUtils.isEmpty(detailAddress) -> {
                toast("Please enter detailed address")
                etDetailAddress.requestFocus()
                return
            }
        }

        val label = when (rgLabel.checkedRadioButtonId) {
            R.id.rb_home -> "Home"
            R.id.rb_company -> "Company"
            R.id.rb_school -> "School"
            else -> "Home"
        }
        val isDefault = switchDefault.isChecked

        btnSave.isEnabled = false
        BackgroundTask.run(
            task = {
                if (isEditMode && editingAddress != null) {
                    editingAddress?.apply {
                        this.name = name
                        this.phone = phone
                        this.province = province
                        this.city = city
                        this.district = district
                        this.detailAddress = detailAddress
                        this.label = label
                        this.isDefault = isDefault
                    }
                    addressManager.updateAddress(editingAddress!!).getOrThrow()
                } else {
                    val address = Address(
                        name = name,
                        phone = phone,
                        province = province,
                        city = city,
                        district = district,
                        detailAddress = detailAddress,
                        label = label,
                        isDefault = isDefault
                    )
                    addressManager.addAddress(address).getOrThrow()
                }
            },
            onSuccess = {
                btnSave.isEnabled = true
                toast(if (isEditMode) "Address updated" else "Address added")
                setResult(RESULT_OK)
                finish()
            },
            onFailure = { error ->
                btnSave.isEnabled = true
                toast(mapSaveErrorMessage(error))
            }
        )
    }

    private fun isValidPhone(phone: String): Boolean = phone.matches(Regex("^[0-9]{10,15}$"))

    private fun mapSaveErrorMessage(error: Throwable): String {
        val raw = error.message.orEmpty()
        val normalized = raw.lowercase()
        return when {
            normalized.contains("failed to connect") || normalized.contains("connect timed out") ->
                "保存失败：无法连接后端服务，请先启动 backend（端口 3001）"
            normalized.contains("http 404") ->
                "保存失败：共享后端接口不存在，请确认已启动 Node 后端（${BackendConfig.DEFAULT_BASE_URL}）"
            normalized.contains("http 5") ->
                "保存失败：后端服务异常，请稍后重试"
            raw.isNotBlank() ->
                "保存失败：$raw"
            else ->
                "保存失败：网络或服务器异常"
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
