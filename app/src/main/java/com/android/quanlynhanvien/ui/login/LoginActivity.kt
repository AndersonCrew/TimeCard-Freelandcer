package com.android.quanlynhanvien.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.quanlynhanvien.BaseActivity
import com.android.quanlynhanvien.Constants
import com.android.quanlynhanvien.MainActivity
import com.android.quanlynhanvien.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity() {
    private var mAuth: FirebaseAuth? = null
    private var btnLogin: TextView?= null
    private var etEmail: EditText?= null
    private var etPassword: EditText?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        btnLogin = findViewById(R.id.btnLogin)
        etPassword = findViewById(R.id.etPassword)
        etEmail = findViewById(R.id.etEmail)

        btnLogin?.setOnClickListener {
            if(etEmail?.text.isNullOrEmpty() || etPassword?.text.isNullOrEmpty()) {
                Toast.makeText(this, "Email hoặc password không được để trống!", Toast.LENGTH_LONG).show()
            } else {
                showProgrss()
                mAuth?.signInWithEmailAndPassword(etEmail?.text.toString(), etPassword?.text.toString())?.addOnCompleteListener {
                    hideProgrss()
                    if(it.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else { Toast.makeText(this, "Email hoặc password không chính xác!", Toast.LENGTH_LONG).show()
                    }
                }?.addOnCanceledListener {
                    hideProgrss()
                    Toast.makeText(this, "Email hoặc password không chính xác!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth?.currentUser
        if(currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}