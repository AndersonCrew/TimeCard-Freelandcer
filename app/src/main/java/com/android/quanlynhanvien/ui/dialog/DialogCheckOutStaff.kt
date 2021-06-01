package com.android.quanlynhanvien.ui.dialog

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.android.quanlynhanvien.R
import com.android.quanlynhanvien.databinding.DialogCheckoutStaffBinding
import com.android.quanlynhanvien.model.User
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class DialogCheckOutStaff(
    context: Context,
    listener: (sanluong: Int) -> Unit
) : Dialog(context) {
   private var binding: DialogCheckoutStaffBinding = DialogCheckoutStaffBinding.inflate(LayoutInflater.from(context))


    init {
        setContentView(binding.root)
        setCancelable(true)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        binding.btnCheckOut.setOnClickListener {
            binding.etsanLuong.error = null
            if(binding.etsanLuong.text.toString().isNullOrEmpty()) {
                binding.etsanLuong.error = "Required!"
            } else {
                dismiss()
                listener.invoke(Integer.parseInt(binding.etsanLuong.text.toString()))
            }
        }

    }
}