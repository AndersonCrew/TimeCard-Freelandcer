package com.android.quanlynhanvien.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import com.android.quanlynhanvien.*
import com.android.quanlynhanvien.model.User
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class InsertStaffActivity : BaseActivity() {
    private var imgBack: ImageView? = null
    private var mAuth: FirebaseAuth? = null
    private var btnInsert: Button? = null
    private var etName: EditText? = null
    private var etEmail: EditText? = null
    private var etMaNV: EditText? = null
    private var etDate: TextView? = null
    private var imgChooseTime: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_staff)
        imgBack = findViewById(R.id.imgBack)
        btnInsert = findViewById(R.id.btnInsert)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etDate = findViewById(R.id.etDate)
        etMaNV = findViewById(R.id.etMaNV)
        imgChooseTime = findViewById(R.id.imgChooseTime)

        imgChooseTime?.setOnClickListener {
            showCalendarDialog()
        }

        imgBack?.setOnClickListener { onBackPressed() }
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        btnInsert?.setOnClickListener {
            showProgress()
            etMaNV?.error = null
            etName?.error = null
            etEmail?.error = null
            etDate?.error = null
            when {
                etMaNV?.text.isNullOrEmpty() -> {
                    etMaNV?.error = "Bắt buộc!"
                    hideProgress()
                }

                etName?.text.isNullOrEmpty() -> {
                    etName?.error = "Bắt buộc!"
                    hideProgress()
                }

                etEmail?.text.isNullOrEmpty() -> {
                    etEmail?.error = "Bắt buộc!"
                    hideProgress()
                }

                !isValidEmail(etEmail?.text.toString()) -> {
                    Toast.makeText(this, "Email không đúng định dạng!", Toast.LENGTH_LONG).show()
                    hideProgress()
                }

                etDate?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Vui lòng chọn ngày sinh!", Toast.LENGTH_LONG).show()
                    hideProgress()
                }

                else -> {
                    hideKeyboard(this)
                    checkMaNV()
                }
            }
        }
    }

    private fun isValidEmail(target: String): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun showCalendarDialog() {
        val mDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val mMonth = Calendar.getInstance().get(Calendar.MONTH)
        val mYear = Calendar.getInstance().get(Calendar.YEAR)
        val datePickerDialog = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                val timePicked = "$dayOfMonth-${monthOfYear + 1}-$year"
                val timeChange = SimpleDateFormat(Constants.TIME_FORMAT).parse(timePicked)
                etDate?.text = SimpleDateFormat(Constants.TIME_FORMAT).format(timeChange)
            },
            mYear,
            mMonth,
            mDay
        )

        datePickerDialog.show()
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private var qrImage : Bitmap? = null
    private fun generateQRCode(uuid: String) {
        val user = User(false, 0, null, "", "", etName?.text.toString(), etDate?.text.toString(), etEmail?.text.toString())
        user.maNV = etMaNV?.text.toString()
        val qrCode = QRGEncoder(Gson().toJson(user), null, QRGContents.Type.TEXT, 500)
        try {
            // Getting QR-Code as Bitmap
            qrImage = qrCode.encodeAsBitmap()
            qrImage?.let {
                //Save Bitmap into Storage
                // Create a Cloud Storage reference from the app

                // Create a Cloud Storage reference from the app
                val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child(uuid)

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
                            user.uuid = uuid

                            addUserDatabase(user)
                        }
                    }
                }
            }

        } catch (e: WriterException) {
            e.printStackTrace()
        }

    }

    private fun checkMaNV() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference.child(Constants.CHILD_NODE_USER)

        myRef.orderByKey().equalTo(etMaNV?.text.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Key exists
                    hideProgress()
                    Toast.makeText(
                        this@InsertStaffActivity,
                        "Mã nhân viên đã tồn tại, vui lòng kiểm tra lại!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    registerAccount()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hideProgress()
            }

        })
    }

    private fun registerAccount() {
        mAuth?.createUserWithEmailAndPassword(etEmail?.text.toString().toLowerCase(), Constants.DEFAULT_PASSWORD)?.addOnCompleteListener {
            if(it.isSuccessful) {
                //Generate QRCode and upload to server
                generateQRCode(it.result?.user?.uid?: etEmail?.text.toString())

            } else {
                hideProgress()
                val message = it.exception?.message?: "Something wrong, Please try again!"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }?.addOnCanceledListener {
            hideProgress()
            Toast.makeText(this, "Something wrong, Please try again!", Toast.LENGTH_LONG).show()
        }
    }

    private fun addUserDatabase(user: User) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constants.CHILD_NODE_USER)
            .child(user.uuid?: "-")
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