package com.android.quanlynhanvien.ui.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.android.quanlynhanvien.BaseActivity
import com.android.quanlynhanvien.Constants
import com.android.quanlynhanvien.R
import com.android.quanlynhanvien.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*


class DetailStaffActivity : BaseActivity() {
    private lateinit var user: User

    private var imgBack: ImageView? = null
    private var btnDelete: ImageView? = null
    private var btnChangeTime: ImageView? = null
    private var btnEdit: Button? = null
    private var etName: EditText? = null
    private var etEmail: EditText? = null
    private var etDate: TextView? = null
    private var etMaNV: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_staff)

        user = intent?.getSerializableExtra(Constants.USER) as User

        imgBack = findViewById(R.id.imgBack)
        btnDelete = findViewById(R.id.imgDelete)
        btnEdit = findViewById(R.id.btnEdit)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etDate = findViewById(R.id.etDate)
        etMaNV = findViewById(R.id.etMaNV)
        btnChangeTime = findViewById(R.id.btnChangeTime)

        etMaNV?.text = user.maNV ?: "-"
        etName?.setText(user.name ?: "-")
        etEmail?.setText(user.email ?: "-")
        etDate?.text = user.birthDay.toString()

        imgBack?.setOnClickListener { onBackPressed() }

        btnDelete?.setOnClickListener { deleteUser() }
        btnEdit?.setOnClickListener { checkEmail() }
        btnChangeTime?.setOnClickListener { showCalendarDialog() }
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

    private fun checkEmail() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference.child(Constants.CHILD_NODE_USER)
        myRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.childrenCount > 0) {
                    for(child in snapshot.children) {
                        var user: User? = child.getValue(User::class.java)
                        user?.email?.let {
                            if(it == etEmail?.text.toString()) {
                                Toast.makeText(
                                    this@DetailStaffActivity,
                                    "Email ???? t???n t???i, vui l??ng ki???m tra l???i!",
                                    Toast.LENGTH_LONG
                                ).show()

                                hideProgress()
                                return
                            }
                        }
                    }

                    editUser()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hideProgress()
                Toast.makeText(
                    this@DetailStaffActivity,
                    "Something wrong! Please try again!",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    private fun deleteUser() {
        hideKeyboard(this)
        showProgress()
        //Delete in DB
        FirebaseDatabase.getInstance().getReference(Constants.CHILD_NODE_USER)
            .child(user.maNV ?: "-").removeValue()
            .addOnCompleteListener { remove ->
                hideProgress()
                if (remove.isSuccessful) {
                    val database = FirebaseDatabase.getInstance()
                    val myRef =
                        database.getReference(Constants.TIMECARD_NODE).child(user.maNV ?: "-")
                    myRef.removeValue().addOnCompleteListener {
                        hideProgress()
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this,
                                "X??a nh??n vi??n th??nh c??ng!",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Vui l??ng ki???m tra l???i k???t n???i m???ng!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Vui l??ng ki???m tra l???i k???t n???i m???ng!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun deleteAuthUser() {
        showProgress()
        val userAuth = FirebaseAuth.getInstance().currentUser

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        val credential = EmailAuthProvider.getCredential(user.email?: "-",Constants.DEFAULT_PASSWORD)
        userAuth.reauthenticate(credential).addOnCompleteListener {
            userAuth.delete().addOnCompleteListener {
                if (it.isSuccessful) {
                    deleteUser()
                } else {
                    hideProgress()
                }
            }
        }
    }


    private fun editUser() {
        showProgress()
        hideKeyboard(this)
        val newName = etName?.text.toString()
        val newEmail = etEmail?.text.toString()
        val newDate = etDate?.text.toString()

        if (newName != user.name || newEmail != user.email || newDate != user.birthDay.toString()) {
            //Delete in DB
            val newUser = User()
            newUser.urlQRCode = user.urlQRCode
            FirebaseDatabase.getInstance().getReference(Constants.CHILD_NODE_USER)
                .child(user.maNV ?: "-").setValue(newUser)
                .addOnCompleteListener { update ->
                    hideProgress()
                    if (update.isSuccessful) {
                        Toast.makeText(
                            this,
                            "C???p nh???t th??nh c??ng!",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Vui l??ng ki???m tra l???i k???t n???i m???ng!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } else {
            hideProgress()
            Toast.makeText(
                this,
                "C???p nh???t th??nh c??ng!",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    companion object {
        fun start(user: User, activity: FragmentActivity) {
            val intent = Intent(activity, DetailStaffActivity::class.java)
            intent.putExtra(Constants.USER, user)
            activity.startActivity(intent)
        }
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}