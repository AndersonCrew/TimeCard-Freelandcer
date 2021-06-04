package com.android.quanlynhanvien.ui.manage

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.quanlynhanvien.Constants
import com.android.quanlynhanvien.R
import com.android.quanlynhanvien.SanLuongAdapter
import com.android.quanlynhanvien.SpinnerStaffAdapter
import com.android.quanlynhanvien.databinding.FragmentReportBinding
import com.android.quanlynhanvien.model.SanLuong
import com.android.quanlynhanvien.model.TimeCard
import com.android.quanlynhanvien.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList


class ReportFragment : Fragment() {
    private var binding: FragmentReportBinding?= null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportBinding.inflate(inflater, container, false)
        initViews()
        return binding?.root
    }

    private var listStaff: ArrayList<User> = arrayListOf()
    private var listString: ArrayList<String> = arrayListOf()
    private var userReport : User?= null
    private var month = 0
    private var year = ""
    private fun initViews() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Loading...")
        progressDialog?.setCancelable(false)
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constants.CHILD_NODE_USER)

        // Read from the database

        // Read from the database
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
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

                listStaff.forEach { staff -> listString.add(staff.name?: "-") }
                if(!listString.isNullOrEmpty()) {
                    binding?.spinnerStaff?.adapter = SpinnerStaffAdapter(requireContext(), R.layout.item_spinner, listString)
                    binding?.spinnerStaff?.setSelection(0)
                    userReport = listStaff[0]
                    binding?.spinnerStaff?.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parentView: AdapterView<*>?,
                            selectedItemView: View,
                            position: Int,
                            id: Long
                        ) {
                            userReport = listStaff[position]
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

        binding?.spinnerMonth?.adapter = SpinnerStaffAdapter(requireContext(), R.layout.item_spinner,
            resources.getStringArray(R.array.arrMonth).toList()
        )
        binding?.spinnerMonth?.setSelection(0)
        binding?.spinnerMonth?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                month = position
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        }


        binding?.spinnerYear?.adapter = SpinnerStaffAdapter(requireContext(), R.layout.item_spinner,
            resources.getStringArray(R.array.arrYear).toList()
        )
        binding?.spinnerYear?.setSelection(0)
        binding?.spinnerYear?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                year = (selectedItemView as TextView).text.toString()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        }

        binding?.btnLoc?.setOnClickListener {
            getReportUser()
        }
    }

    private var listReport: ArrayList<SanLuong> = arrayListOf()
    private var adapter: SanLuongAdapter?= null
    private fun getReportUser() {

        showProgrss()
        binding?.tvTime?.text = "Ngày"
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constants.TIMECARD_NODE).child(userReport?.maNV?: "-")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                listReport = arrayListOf()
                adapter = SanLuongAdapter((arrayListOf()))
                binding?.rvReport?.adapter = adapter
                binding?.rvReport?.visibility = View.GONE
                binding?.csT?.visibility = View.GONE
                dataSnapshot.ref.child(year).child((month + 1).toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //Child Day
                        for(childDay in snapshot.children) {
                            childDay.key?.let {
                                val sanLuong = SanLuong()
                                sanLuong.time = childDay.key
                                var sanluongKg = 0
                                childDay.ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for(childTimeCard in snapshot.children) {
                                            val timeCard: TimeCard? = childTimeCard.getValue(
                                                TimeCard::class.java)
                                            timeCard?.let {timeCardDTO ->
                                                sanluongKg += timeCardDTO.sanluong?: 0
                                            }
                                        }

                                        sanLuong.sanluong = sanluongKg
                                        listReport.add(sanLuong)
                                        adapter?.addItem(sanLuong)

                                        hideProgrss()
                                        if(listReport.size > 0) {
                                            binding?.csT?.visibility = View.VISIBLE
                                            binding?.rvReport?.visibility = View.VISIBLE
                                        } else {
                                            binding?.csT?.visibility = View.GONE
                                            binding?.rvReport?.visibility = View.GONE
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        hideProgrss()
                                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                                    }

                                })
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        hideProgrss()
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                    }

                })


                hideProgrss()
            }

            override fun onCancelled(error: DatabaseError) {
                hideProgrss()
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private var progressDialog: ProgressDialog?= null

    private fun showProgrss() {
        if(progressDialog != null && progressDialog?.isShowing == false) {
            progressDialog?.show()
        }
    }


    private fun hideProgrss() {
        if(progressDialog != null && progressDialog?.isShowing == true) {
            progressDialog?.cancel()
        }
    }

}