package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.myapplication.AddressListActivity
import com.example.myapplication.CouponActivity
import com.example.myapplication.CustomerServiceActivity
import com.example.myapplication.EditProfileActivity
import com.example.myapplication.LoginActivity
import com.example.myapplication.OrderListActivity
import com.example.myapplication.R
import com.example.myapplication.SettingsActivity
import com.example.myapplication.util.UserProfileManager

class UserFragment : Fragment() {
    private var layoutOrders: LinearLayout? = null
    private var layoutAddress: LinearLayout? = null
    private var layoutCoupon: LinearLayout? = null
    private var layoutService: LinearLayout? = null
    private var layoutSettings: LinearLayout? = null
    private var btnLogout: Button? = null
    private var ivEditProfile: ImageView? = null
    private var tvUserName: TextView? = null
    private var tvUserPhone: TextView? = null
    private var userProfileManager: UserProfileManager? = null
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == android.app.Activity.RESULT_OK) {
            loadUserProfile()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        initViews(view)
        setupListeners()
        return view
    }

    private fun initViews(view: View) {
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserPhone = view.findViewById(R.id.tv_user_phone)
        ivEditProfile = view.findViewById(R.id.iv_edit_profile)
        layoutOrders = view.findViewById(R.id.layout_orders)
        layoutAddress = view.findViewById(R.id.layout_address)
        layoutCoupon = view.findViewById(R.id.layout_coupon)
        layoutService = view.findViewById(R.id.layout_service)
        layoutSettings = view.findViewById(R.id.layout_settings)
        btnLogout = view.findViewById(R.id.btn_logout)
        userProfileManager = context?.let { UserProfileManager.getInstance(it) }
        loadUserProfile()
    }

    private fun setupListeners() {
        ivEditProfile?.setOnClickListener {
            runCatching {
                val intent = Intent(activity, EditProfileActivity::class.java)
                editProfileLauncher.launch(intent)
            }.onFailure {
                showToast("Cannot open edit profile page")
            }
        }

        layoutOrders?.setOnClickListener {
            runCatching {
                startActivity(Intent(activity, OrderListActivity::class.java))
            }.onFailure {
                showToast("Cannot open orders page")
            }
        }

        layoutAddress?.setOnClickListener {
            runCatching {
                startActivity(Intent(activity, AddressListActivity::class.java))
            }.onFailure {
                showToast("Cannot open address management page")
            }
        }

        layoutCoupon?.setOnClickListener {
            runCatching {
                startActivity(Intent(activity, CouponActivity::class.java))
            }.onFailure {
                showToast("Cannot open coupons page")
            }
        }

        layoutService?.setOnClickListener {
            runCatching {
                startActivity(Intent(activity, CustomerServiceActivity::class.java))
            }.onFailure {
                showToast("Cannot open customer service page")
            }
        }

        layoutSettings?.setOnClickListener {
            runCatching {
                startActivity(Intent(activity, SettingsActivity::class.java))
            }.onFailure {
                showToast("Cannot open settings page")
            }
        }

        btnLogout?.setOnClickListener { showLogoutDialog() }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val manager = userProfileManager ?: return
        tvUserName?.text = manager.getUserName()
        tvUserPhone?.text = manager.getUserPhone()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutDialog() {
        val hostActivity = activity ?: return
        AlertDialog.Builder(hostActivity)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Logout") { _, _ ->
                val success = hostActivity.getSharedPreferences(PREFS_AUTH, android.content.Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_LOGGED_IN, false)
                    .commit()

                if (!success) {
                    showToast("Logout failed, please try again")
                    return@setPositiveButton
                }

                runCatching {
                    val intent = Intent(hostActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    hostActivity.startActivity(intent)
                }.onFailure {
                    showToast("Cannot open login page")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        private const val PREFS_AUTH = "auth_prefs"
        private const val KEY_LOGGED_IN = "logged_in"
    }
}
