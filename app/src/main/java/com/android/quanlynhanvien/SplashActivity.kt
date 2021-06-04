package com.android.quanlynhanvien

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.android.quanlynhanvien.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if(SharePreferencesUtils(this).getBoolean(Constants.IS_LOGIN, false)) {
                if(SharePreferencesUtils(this).getUser() != null){
                    startActivity(Intent(this@SplashActivity, MainStaffActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                finish()
            }
        }, 1500)
    }
}