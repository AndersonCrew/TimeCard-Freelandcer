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
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.quanlynhanvien.R
import com.android.quanlynhanvien.model.User
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class ShowQrDialog(context: Context,private val user: User, private val listener: () -> Unit): Dialog(context) {
    private var imgQrCode: ImageView?= null
    private var btnSave: Button?= null
    private val viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.IO + viewModelJob)
    init {
        setContentView(R.layout.dialog_show_qr)
        setCancelable(true)
        imgQrCode = findViewById(R.id.imgQrCode)
        btnSave = findViewById(R.id.btnSave)
        Picasso.get().load(user.urlQRCode?: "").into(imgQrCode)

        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        btnSave?.setOnClickListener {
            onSaveFile()
        }
    }

    fun onSaveFile() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            saveBitmap()
        } else {
            listener.invoke()
        }
    }

    private fun saveBitmap() {
        uiScope.launch {
            val path: String = Environment.getExternalStorageDirectory().toString()
            var fOut: OutputStream? = null
            val counter = 0
            val file = File(
                path,
                "QRCode_${user.name?: "-"}.jpg"
            ) // the File to save , append increasing numeric counter to prevent files from getting overwritten.

            fOut = FileOutputStream(file)

            if(getBitmapFromURL(user.urlQRCode) != null) {
                val bitmap = getBitmapFromURL(user.urlQRCode)
                bitmap?.compress(
                    Bitmap.CompressFormat.JPEG,
                    85,
                    fOut
                ) // saving the Bitmap to a file compressed as a JPEG with 85% compression rate

                fOut.flush() // Not really required

                fOut.close() // do not forget to close the stream


                MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    file.absolutePath,
                    file.name,
                    file.name
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Save file successfully!", Toast.LENGTH_LONG).show()
                    dismiss()
                }
            }

        }

    }

    private fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            null
        }
    }
}