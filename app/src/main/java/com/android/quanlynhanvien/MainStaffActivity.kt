package com.android.quanlynhanvien

import android.content.Intent
import android.os.Bundle
import com.android.quanlynhanvien.databinding.ActivityMainStaffBinding
import com.android.quanlynhanvien.model.User
import com.android.quanlynhanvien.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class MainStaffActivity : BaseActivity() {
    private var binding: ActivityMainStaffBinding?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainStaffBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val sharePref = SharePreferencesUtils(this)
        sharePref.getUser()?.let {
            bindData(it)
        }

        binding?.imgLogout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            SharePreferencesUtils(this).also {
                it.saveUser(null)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun bindData(user: User) {
        binding?.imgAvatar?.let {
            user.avatar?.let { url ->
                Picasso.get().load(url).into(it)
            }

        }

        binding?.tvName?.text = user.name?: "<Empty>"
        binding?.tvFullName?.text = user.name?: "<Empty>"
        binding?.tvEmail?.text = user.email?: "<Empty>"
        binding?.tvDate?.text = user.birthDay?: "<Empty>"
        binding?.tvAddress?.text = user.address?: "<Empty>"
        binding?.tvReligion?.text = user.dantoc?: "<Empty>"
        binding?.tvGender?.text = if(user.gioitinh == 0) "Nam" else "Nữ"
    }
}