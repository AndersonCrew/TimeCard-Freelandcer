package com.android.quanlynhanvien

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity: AppCompatActivity() {
    private var progressDialog: ProgressDialog?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(this)
        progressDialog?.setCancelable(false)
        progressDialog?.setMessage("Loading...")
        supportActionBar?.hide()
    }

     fun showProgress() {
        if(progressDialog != null && progressDialog?.isShowing == false) {
            progressDialog?.show()
        }
    }


     fun hideProgress() {
        if(progressDialog != null && progressDialog?.isShowing == true) {
            progressDialog?.cancel()
        }
    }
}