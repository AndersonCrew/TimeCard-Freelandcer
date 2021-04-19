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
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.android.quanlynhanvien.R
import com.android.quanlynhanvien.model.User
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class TimeCardDialog(
    listChosen: ArrayList<Int>,
    context: Context,
    private val list: ArrayList<String>,
    listener: (ArrayList<Int>) -> Unit
) : Dialog(context) {
    private var llFilter: LinearLayout? = null
    private var btnFilter: Button? = null

    private val viewModelJob = Job()

    init {
        setContentView(R.layout.dialog_timecard)
        setCancelable(true)
        llFilter = findViewById(R.id.llFilter)
        btnFilter = findViewById(R.id.btnFilter)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (!list.isNullOrEmpty()) {
            for (str in list) {
                var textView = TextView(context)
                textView.text = str
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                params.leftMargin = 10
                params.rightMargin = 10
                textView.setPadding(20, 10, 20, 10)
                textView.layoutParams = params
                textView.id = list.indexOf(str)
                if (listChosen.contains(list.indexOf(str))) {
                    textView.setBackgroundResource(R.drawable.bg_item)
                } else {
                    textView.setBackgroundResource(R.drawable.bg_item_not)
                }

                textView.setOnClickListener {
                    if (listChosen.contains(list.indexOf(str))) {
                        listChosen.remove(list.indexOf(str))
                        textView.setBackgroundResource(R.drawable.bg_item_not)
                    } else {
                        listChosen.add(list.indexOf(str))
                        textView.setBackgroundResource(R.drawable.bg_item)
                    }
                }

                llFilter?.addView(textView)
            }
        }

        btnFilter?.setOnClickListener {
            listener.invoke(listChosen)
            dismiss()
        }
    }
}