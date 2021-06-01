package com.android.quanlynhanvien

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.android.quanlynhanvien.databinding.ActivityEditProfileBinding
import com.android.quanlynhanvien.model.User
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.util.*



class EditProfileActivity : BaseActivity() {
    private var binding: ActivityEditProfileBinding?= null
    private val REQUEST_CODE_STORAGE = 101
    private val REQUEST_CODE_IMAGE = 102
    private var bitmapAvatar: Bitmap?= null
    private var user: User ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.imgBack?.setOnClickListener { onBackPressed() }
        binding?.cardAvatar?.setOnClickListener {
            if(PermissionUtil.checkPermissions(this, PermissionUtil.permissionsStorage)) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"),
                    REQUEST_CODE_IMAGE
                )
            } else {
                PermissionUtil.requestPermissions(this, REQUEST_CODE_STORAGE, PermissionUtil.permissionsStorage)
            }
        }

        val sharePref = SharePreferencesUtils(this)
        sharePref.getUser()?.let {
            bindData(it)
            user = it
        }
    }

    private fun bindData(user: User) {
        binding?.imgAvatar?.let {
            user.avatar?.let { url ->
                Picasso.get().load(url).into(it)
            }

        }

        binding?.tvFullName?.setText(if(!user.name.isNullOrEmpty()) user.name else "")
        binding?.tvEmail?.text = if(!user.email.isNullOrEmpty()) user.email else ""
        binding?.tvDate?.text = if(!user.birthDay.isNullOrEmpty()) user.birthDay else ""
        binding?.tvAddress?.setText(if(!user.address.isNullOrEmpty()) user.address else "")
        binding?.tvPhone?.setText(if(!user.phoneNumber.isNullOrEmpty()) user.phoneNumber else "")
        binding?.tvReligion?.setText(if(!user.dantoc.isNullOrEmpty()) user.dantoc else "")
        binding?.tvGender?.setText(if(user.gioitinh == 0) "Nam" else "Nữ")

        binding?.imgChooseTime?.setOnClickListener {
            chooseDate()
        }

        binding?.btnUpdate?.setOnClickListener {
            if(binding?.tvFullName?.text.toString().isNullOrEmpty()) {
                Toast.makeText(this, resources.getString(R.string.reuired_fullName), Toast.LENGTH_LONG).show()
            } else {
                showProgress()
                if(bitmapAvatar != null) {
                    upLoadAvatar()
                } else {
                    editProfile()
                }
            }
        }
    }

    private fun upLoadAvatar() {
        //Save Bitmap into Storage
        // Create a Cloud Storage reference from the app

        // Create a Cloud Storage reference from the app
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child(Constants.STORAGE_IMAGES).child(System.currentTimeMillis().toString())

        val baos = ByteArrayOutputStream()
        bitmapAvatar!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data: ByteArray = baos.toByteArray()

        val uploadTask: UploadTask = storageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener {

            storageRef.downloadUrl.addOnSuccessListener {url ->
                url?.let {
                    val downloadUrl: Uri = url
                    user?.avatar = downloadUrl.toString()

                    editProfile()
                }
            }
        }
    }

    private fun editProfile() {
        user?.apply {
            name = binding?.tvFullName?.text.toString()
            email = binding?.tvEmail?.text.toString()
            birthDay = binding?.tvDate?.text.toString()
            address = binding?.tvAddress?.text.toString()
            dantoc = binding?.tvReligion?.text.toString()
            phoneNumber = binding?.tvPhone?.text.toString()
        }

        FirebaseDatabase.getInstance().getReference(Constants.CHILD_NODE_USER)
            .child(user?.uuid ?: "-").setValue(user)
            .addOnCompleteListener { update ->
                hideProgress()
                if (update.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Cập nhật thành công!",
                        Toast.LENGTH_LONG
                    ).show()
                    SharePreferencesUtils(this).saveUser(user)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Vui lòng kiểm tra lại kết nối mạng!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }


    private var timeChange = ""
    @SuppressLint("SimpleDateFormat")
    private fun chooseDate() {
        val mDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val mMonth = Calendar.getInstance().get(Calendar.MONTH)
        val mYear = Calendar.getInstance().get(Calendar.YEAR)
        val datePickerDialog = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                timeChange = "$dayOfMonth-${monthOfYear + 1}-$year"
                binding?.tvDate?.text = timeChange
            },
            mYear,
            mMonth,
            mDay
        )

        datePickerDialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE_STORAGE) {
            if (PermissionUtil.checkPermissions(this, PermissionUtil.permissionsStorage)) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"),
                    REQUEST_CODE_IMAGE
                )
            } else {
                Toast.makeText(this, "External Storage permission needed. Please allow in App Settings for additional functionality", Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let {
                binding?.imgAvatar?.setImageURI(it)
                 bitmapAvatar = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
            }
        }
    }
}