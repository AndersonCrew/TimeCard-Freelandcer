package com.android.quanlynhanvien.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.android.quanlynhanvien.BaseActivity
import com.android.quanlynhanvien.Constants
import com.android.quanlynhanvien.R
import com.android.quanlynhanvien.model.TimeCard
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*


class DetailTimeCardActivity : BaseActivity() {
    private lateinit var timeCard: TimeCard
    private var etEmail: TextView?= null
    private var tvName: TextView?= null
    private var tvDate: TextView?= null
    private var tvMaNV: TextView?= null
    private var btnDelete: ImageView?= null
    private var btnEdit: TextView?= null
    private var imgBack: ImageView?= null
    private var tvChooseTime: ImageView?= null
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_time_card)

        tvDate = findViewById(R.id.etDate)
        tvName = findViewById(R.id.etName)
        btnDelete = findViewById(R.id.imgDelete)
        btnEdit = findViewById(R.id.btnEdit)
        imgBack = findViewById(R.id.imgBack)
        tvChooseTime = findViewById(R.id.btnChooseTime)
        tvMaNV = findViewById(R.id.spinnerMaNV)
        etEmail = findViewById(R.id.etEmail)


        intent?.let {
            timeCard = it.getSerializableExtra(Constants.TIMECARD) as TimeCard
        }

        imgBack?.setOnClickListener { onBackPressed() }
        btnDelete?.setOnClickListener {
            deleteTimeCard()
        }

        btnEdit?.setOnClickListener {
            if(timeChange != 0L) {
                editTimeCard()
            } else {
                finish()
            }

        }

        tvChooseTime?.setOnClickListener {
            chooseTime()
        }

        etEmail?.text = timeCard.user?.email?: "-"
        tvMaNV?.text = timeCard.user?.maNV?: "-"
        tvName?.text = timeCard.user?.name?: "-"
        val date = Date()
        date.time = timeCard.timeCheck?: 0
        tvDate?.text = SimpleDateFormat(Constants.HOUR_TIME_FORMAT).format(date)
    }

    companion object {
        fun start(timeCard: TimeCard, activity: FragmentActivity) {
            val intent = Intent(activity, DetailTimeCardActivity::class.java)
            intent.putExtra(Constants.TIMECARD, timeCard)
            activity.startActivity(intent)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun deleteTimeCard() {
        showProgrss()
        val date = Date()
        date.time = timeCard.timeCheck?: 0
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constants.TIMECARD_NODE).child(timeCard.user?.maNV?: "-").child(timeCard.timeCheck.toString())
        myRef.removeValue().addOnCompleteListener {
            hideProgrss()
            if(it.isSuccessful) {
                Toast.makeText(this, "Xóa chấm công thành công!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Vui lòng kiểm tra lại kết nối mạng!", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun editTimeCard() {

        val date = Date()
        date.time = timeCard.timeCheck?: 0
        val timeStr = SimpleDateFormat(Constants.HOUR_TIME_FORMAT).format(date)
        if(timeStr != tvDate?.text){
            showProgrss()
            val database = FirebaseDatabase.getInstance()
            database.getReference(Constants.TIMECARD_NODE).child(timeCard.user?.maNV?: "-").child(timeCard.timeCheck.toString()).removeValue().addOnCompleteListener {
                if(it.isSuccessful) {
                    val newTimeCard = TimeCard(timeCard.user, timeChange)
                    val myRef = database.getReference(Constants.TIMECARD_NODE).child(timeCard.user?.maNV?: "-").child(timeChange.toString())
                    myRef.setValue(newTimeCard).addOnCompleteListener {
                        hideProgrss()
                        if(it.isSuccessful) {
                            Toast.makeText(this, "Cập nhật chấm công thành công!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Vui lòng kiểm tra lại kết nối mạng!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }


        } else {
            finish()
        }

    }

    private var timeChange = 0L
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
}