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
                if(binding?.etEmail?.text.toString() == Constants.EMAIL_ADMIN_DEFAULT) {
                    mAuth?.signInWithEmailAndPassword(binding?.etEmail?.text.toString(), binding?.etPassword?.text.toString())?.addOnCompleteListener {
                        if(it.isSuccessful) {
                            hideProgress()
                            SharePreferencesUtils(this).setBoolean(Constants.IS_LOGIN, true)
                            SharePreferencesUtils(this).saveUser(null)
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            hideProgress()
                            Toast.makeText(this, "Email or Password is not match!", Toast.LENGTH_LONG).show()
                        }
                    }?.addOnCanceledListener {
                        hideProgress()
                        Toast.makeText(this, "Email or Password is not match!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    logInUser ()
                }

            }
        }

        binding?.tvRegister?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun logInUser() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference.child(Constants.CHILD_NODE_USER)
        myRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.childrenCount > 0) {
                    for(child in snapshot.children) {
                        var user: User? = child.getValue(User::class.java)
                        if(user?.email == binding?.etEmail?.text.toString() && user.password == binding?.etPassword?.text.toString()) {
                            hideProgress()
                            SharePreferencesUtils(this@LoginActivity).saveUser(user)
                            SharePreferencesUtils(this@LoginActivity).setBoolean(Constants.IS_LOGIN, true)
                            startActivity(Intent(this@LoginActivity, MainStaffActivity::class.java))
                            return
                        }
                    }

                    hideProgress()
                    Toast.makeText(this@LoginActivity, "Email or Password is not match!", Toast.LENGTH_LONG).show()
                } else {
                    hideProgress()
                    Toast.makeText(this@LoginActivity, "Email or Password is not match!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hideProgress()
                Toast.makeText(
                    this@LoginActivity,
                    "Something wrong! Please try again!",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }
}