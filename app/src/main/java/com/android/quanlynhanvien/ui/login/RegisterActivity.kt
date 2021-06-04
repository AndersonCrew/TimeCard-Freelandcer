package com.android.quanlynhanvien.ui.login

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import com.android.quanlynhanvien.*
import com.android.quanlynhanvien.databinding.ActivityRegisterBinding
import com.android.quanlynhanvien.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.google.zxing.WriterException
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class RegisterActivity : BaseActivity() {
    private var binding: ActivityRegisterBinding?= null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        initEvents()
    }

    private fun initEvents() {
        mAuth = FirebaseAuth.getInstance()
        binding?.btnRegister?.setOnClickListener {
            showProgress()
            if(checkValidation().isNullOrEmpty()) {
                checkEmail()
            } else {
                hideProgress()
                Toast.makeText(this, checkValidation(), Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun checkValidation(): String {
        var error = ""

        when {
            binding?.etFullName?.text.isNullOrEmpty() -> {
                error = resources.getString(R.string.reuired_fullName)
            }

            binding?.etEmail?.text.isNullOrEmpty() -> {
                error = resources.getString(R.string.reuired_email)
            }

            binding?.etPhone?.text.isNullOrEmpty() -> {
                error = resources.getString(R.string.required_phone)
            }

            binding?.etPassword?.text.isNullOrEmpty() -> {
                error = resources.getString(R.string.required_password)
            }

            binding?.etConfirmPass?.text.isNullOrEmpty() -> {
                error = resources.getString(R.string.required_password)
            }

            binding?.etPassword?.text?.toString()?.equals(binding?.etConfirmPass?.text?.toString()) == false -> {
                error = resources.getString(R.string.password_not_match)
            }
        }

        return error
    }


    private var qrImage : Bitmap? = null
    private fun generateQRCode() {
        val user = User(false, 0, null, "", "", binding?.etFullName?.text.toString(),
            "", binding?.etEmail?.text.toString())
        user.maNV = Random.nextInt(0, 100000).toString()
        user.password = binding?.etPassword?.text.toString()
        val qrCode = QRGEncoder(Gson().toJson(user), null, QRGContents.Type.TEXT, 500)
        try {
            // Getting QR-Code as Bitmap
            qrImage = qrCode.encodeAsBitmap()
            qrImage?.let {
                //Save Bitmap into Storage
                // Create a Cloud Storage reference from the app

                // Create a Cloud Storage reference from the app
                val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child(user.maNV?: "-")

                val baos = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data: ByteArray = baos.toByteArray()

                val uploadTask: UploadTask = storageRef.putBytes(data)
                uploadTask.addOnFailureListener {
                    // Handle unsuccessful uploads
                }.addOnSuccessListener {

                    storageRef.downloadUrl.addOnSuccessListener {url ->
                        url?.let {
                            val downloadUrl: Uri = url
                            user.urlQRCode = downloadUrl.toString()

                            addUserDatabase(user)
                        }
                    }
                }
            }

        } catch (e: WriterException) {
            e.printStackTrace()
        }

    }

    private fun checkEmail() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference.child(Constants.CHILD_NODE_USER)
        myRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.childrenCount > 0) {
                    for(child in snapshot.children) {
                        var user: User? = child.getValue(User::class.java)
                        user?.email?.let {
                            if(it == binding?.etEmail?.text.toString()) {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Email đã tồn tại, vui lòng kiểm tra lại!",
                                    Toast.LENGTH_LONG
                                ).show()

                                hideProgress()
                                return
                            }
                        }
                    }

                    generateQRCode()
                } else {
                    generateQRCode()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hideProgress()
                Toast.makeText(
                    this@RegisterActivity,
                    "Something wrong! Please try again!",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    private fun addUserDatabase(user: User) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constants.CHILD_NODE_USER)
            .child(user.maNV?: "-")
        myRef.setValue(user).addOnCompleteListener {
            hideProgress()
            if (it.isSuccessful) {
                Toast.makeText(
                    this,
                    "Insert Staff successfully!",
                    Toast.LENGTH_LONG
                ).show()

                SharePreferencesUtils(this).saveUser(user)
                startActivity(Intent(this, MainStaffActivity::class.java))
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Insert Staff Fail, Please try again!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.addOnCanceledListener {
            hideProgress()
            Toast.makeText(
                this,
                "Insert Staff Fail, Please try again!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}