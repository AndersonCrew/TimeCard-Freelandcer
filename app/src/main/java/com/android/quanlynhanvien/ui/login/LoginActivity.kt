package com.android.quanlynhanvien.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.android.quanlynhanvien.*
import com.android.quanlynhanvien.databinding.ActivityLoginBinding
import com.android.quanlynhanvien.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : BaseActivity() {
    private var mAuth: FirebaseAuth? = null
    private var binding: ActivityLoginBinding?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        initEvents()
    }

    private fun initEvents() {
        binding?.btnLogin?.setOnClickListener {
            if(binding?.etEmail?.text.isNullOrEmpty() || binding?.etPassword?.text.isNullOrEmpty()) {
                Toast.makeText(this, "Email or Password is empty!", Toast.LENGTH_LONG).show()
            } else {
                showProgress()
                mAuth?.signInWithEmailAndPassword(binding?.etEmail?.text.toString(), binding?.etPassword?.text.toString())?.addOnCompleteListener {
                    if(it.isSuccessful) {
                        if(binding?.etEmail?.text.toString() != Constants.EMAIL_ADMIN_DEFAULT) {
                            getDataStaff(it.result?.user?.uid?:"")
                        } else {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        hideProgress()
                        Toast.makeText(this, "Email or Password is not match!", Toast.LENGTH_LONG).show()
                    }
                }?.addOnCanceledListener {
                    hideProgress()
                    Toast.makeText(this, "Email or Password is not match!", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding?.tvRegister?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun getDataStaff(uuid: String) {
        val database = FirebaseDatabase.getInstance()
        database.getReference(Constants.CHILD_NODE_USER)
            .child(uuid).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    hideProgress()
                    Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        hideProgress()
                        val share = SharePreferencesUtils(this@LoginActivity)
                        share.saveUser(it)
                        startActivity(Intent(this@LoginActivity, MainStaffActivity::class.java))
                        finish()
                    }
                }

            })
    }
}