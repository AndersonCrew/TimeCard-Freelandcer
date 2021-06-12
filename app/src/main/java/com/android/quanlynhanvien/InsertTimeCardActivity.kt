package com.android.quanlynhanvien

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.android.quanlynhanvien.model.TimeCard
import com.android.quanlynhanvien.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*


class InsertTimeCardActivity : BaseActivity() {

    private var spinnerStaff: Spinner?= null
    private var tvDate: TextView?= null
    private var tvName: TextView?= null
    private var tvEmail: TextView?= null
    private var etMaNV: TextView?= null
    private var btnInsert: Button?= null
    private var btnChooseTime: ImageView?= null
    private var imgBack: ImageView?= null
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert_time_card)

        spinnerStaff = findViewById(R.id.spinnerMaNV)
        tvDate = findViewById(R.id.etDate)
        tvName = findViewById(R.id.etName)
        tvEmail = findViewById(R.id.etEmail)
        etMaNV = findViewById(R.id.etMaNV)
        btnInsert = findViewById(R.id.btnInsert)
        imgBack = findViewById(R.id.imgBack)
        btnChooseTime = findViewById(R.id.btnChooseTime)

        imgBack?.setOnClickListener { onBackPressed() }
        btnChooseTime?.setOnClickListener {
            chooseTime()
        }

        btnInsert?.setOnClickListener {
            insertTimeCard()
        }

        initView()
    }

    private var listStaff: ArrayList<User> = arrayListOf()
    private var listString: ArrayList<String> = arrayListOf()
    private var positionUserChosen = 0
    private fun initView() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constants.CHILD_NODE_USER)

        // Read from the database

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                listStaff = arrayListOf()
                listString = arrayListOf()
                for(child in dataSnapshot.children) {
                    val user: User? = child.getValue(User::class.java)
                    user?.let {
                        listStaff.add(it)
                    }
                }

                listStaff.forEach { staff -> listString.add(staff.maNV?: "-") }
                if(!listString.isNullOrEmpty()) {
                    spinnerStaff?.adapter = SpinnerStaffAdapter(this@InsertTimeCardActivity, R.layout.item_spinner, listString)
                    spinnerStaff?.setSelection(0)
                    spinnerStaff?.onItemSelectedListener = object : OnItemSelectedListener {
                        override fun onItemSelected(
                            parentView: AdapterView<*>?,
                            selectedItemView: View,
                            position: Int,
                            id: Long
                        ) {
                            positionUserChosen = position
                            bindDate(listStaff[position])
                        }

                        override fun onNothingSelected(parentView: AdapterView<*>?) {
                            // your code here
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun bindDate(user: User) {
        etMaNV?.text = user.maNV
        tvName?.text = user.name
        tvEmail?.text = user.name
    }

    @SuppressLint("SimpleDateFormat")
    private fun chooseTime() {
        val date = Date(System.currentTimeMillis())
        val mHour = SimpleDateFormat("hh").format(date)
        val mMinute = SimpleDateFormat("mm").format(date)
        val timePickerDialog = TimePickerDialog(
            this,
            { view, hourOfDay, minute ->
                chooseDate(hourOfDay, minute)
            },
            Integer.parseInt(mHour),
            Integer.parseInt(mMinute),
            false
        )
        timePickerDialog.show()
    }

    private var timeChange = System.currentTimeMillis()
    @SuppressLint("SimpleDateFormat")
    private fun chooseDate(hour: Int, minute: Int) {
        val mDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val mMonth = Calendar.getInstance().get(Calendar.MONTH)
        val mYear = Calendar.getInstance().get(Calendar.YEAR)
        val datePickerDialog = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                val timePicked = "$hour: $minute $dayOfMonth-${monthOfYear + 1}-$year"
                timeChange = SimpleDateFormat(Constants.HOUR_TIME_FORMAT).parse(timePicked).time
                tvDate?.text = SimpleDateFormat(Constants.HOUR_TIME_FORMAT).format(SimpleDateFormat(Constants.HOUR_TIME_FORMAT).parse(timePicked))
            },
            mYear,
            mMonth,
            mDay
        )

        datePickerDialog.show()
    }


    private fun insertTimeCard() {
        if(listStaff.isNullOrEmpty()) {
            Toast.makeText(this, "Danh sách nhân viên rỗng!", Toast.LENGTH_LONG).show()
            return
        }

        if(tvDate?.text.isNullOrEmpty()) {
            Toast.makeText(this, "Vui lòng chọn thời gian!", Toast.LENGTH_LONG).show()
            return
        }


        showProgress()
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        val timeCard = TimeCard()
        timeCard.apply {
            user = listStaff[positionUserChosen]
            timeCheck = System.currentTimeMillis()
            typeCheck = 0
        }

        FirebaseDatabase.getInstance().getReference(Constants.TIMECARD_NODE)
            .child(listStaff[positionUserChosen].maNV ?: "-")
            .child(year.toString())
            .child(month.toString())
            .child(day.toString())
            .child(Constants.CHECK_IN).setValue(timeCard)
            .addOnCompleteListener { update ->
                hideProgress()
                if (update.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Chấm công thành công!",
                        Toast.LENGTH_LONG
                    ).show()

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
}