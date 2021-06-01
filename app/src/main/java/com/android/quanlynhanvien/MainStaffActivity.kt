package com.android.quanlynhanvien

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.android.quanlynhanvien.databinding.ActivityMainStaffBinding
import com.android.quanlynhanvien.model.TimeCard
import com.android.quanlynhanvien.model.User
import com.android.quanlynhanvien.ui.dialog.DialogCheckOutStaff
import com.android.quanlynhanvien.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.util.*

class MainStaffActivity : BaseActivity() {
    private var binding: ActivityMainStaffBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainStaffBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.imgLogout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            SharePreferencesUtils(this).also {
                it.saveUser(null)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        binding?.btnEdit?.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding?.btnCheckOut?.setOnClickListener {
            DialogCheckOutStaff(this) {
                showProgress()
                checkOutStaff(it)
            }.show()
        }

    }

    private fun checkOutStaff(mSanluong: Int) {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        val timeCard = TimeCard()
        timeCard.apply {
            user = SharePreferencesUtils(this@MainStaffActivity).getUser()
            timeCheck = System.currentTimeMillis()
            typeCheck = 1
            sanluong = mSanluong
        }

        val myRef = FirebaseDatabase.getInstance().getReference(Constants.TIMECARD_NODE)
            .child(SharePreferencesUtils(this@MainStaffActivity).getUser()?.uuid ?: "-")
            .child(year.toString())
            .child(month.toString())
            .child(day.toString())


        val key = myRef.push().key ?: return
        myRef.child(key).setValue(timeCard)
            .addOnCompleteListener { update ->
                hideProgress()
                if (update.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Chấm công thành công!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Vui lòng kiểm tra lại kết nối mạng!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onResume() {
        super.onResume()

        val sharePref = SharePreferencesUtils(this)
        sharePref.getUser()?.let {
            bindData(it)
        }
    }

    private fun bindData(user: User) {
        binding?.imgAvatar?.let {
            user.avatar?.let { url ->
                Picasso.get().load(url).into(it)
            }

        }

        binding?.tvName?.text = user.name ?: ""
        binding?.tvFullName?.text = user.name ?: ""
        binding?.tvEmail?.text = user.email ?: ""
        binding?.tvDate?.text = user.birthDay ?: ""
        binding?.tvAddress?.text = user.address ?: ""
        binding?.tvReligion?.text = user.dantoc ?: ""
        binding?.tvPhone?.text = user.phoneNumber ?: ""
        binding?.tvGender?.text = if (user.gioitinh == 0) "Nam" else "Nữ"
    }
}