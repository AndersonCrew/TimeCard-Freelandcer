package com.android.quanlynhanvien.ui.manage

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.quanlynhanvien.Constants
import com.android.quanlynhanvien.InsertTimeCardActivity
import com.android.quanlynhanvien.R
import com.android.quanlynhanvien.model.TimeCard
import com.android.quanlynhanvien.model.TimeCardDate
import com.android.quanlynhanvien.model.User
import com.android.quanlynhanvien.ui.DetailTimeCardActivity
import com.android.quanlynhanvien.ui.InsertStaffActivity
import com.android.quanlynhanvien.ui.detail.DetailStaffActivity
import com.android.quanlynhanvien.ui.dialog.ShowQrDialog
import com.android.quanlynhanvien.ui.dialog.TimeCardDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageTimeCardFragment : Fragment() {

    private var cardInsert: ImageView?= null
    private var rvTimeCard: RecyclerView?= null
    private var tvNoData: TextView?= null
    private lateinit var adapter: TimeCardDateAdapter.TimeCardAdapter
    private var progressDialog: ProgressDialog?= null
    private var etSearch: EditText?= null
    private var listTimeCard: ArrayList<TimeCard> = arrayListOf()
    private var imgFilter: ImageView?= null

    private var listFiler : ArrayList<String> = arrayListOf()
    private var listFilerChosen : ArrayList<Int> = arrayListOf()
    val uiScope = CoroutineScope(Dispatchers.IO)
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_gallery, container, false)

        cardInsert = root.findViewById(R.id.imgInsert)
        rvTimeCard = root.findViewById(R.id.rvTimeCard)
        tvNoData = root.findViewById(R.id.tvNodata)
        etSearch = root.findViewById(R.id.etSearch)
        imgFilter = root.findViewById(R.id.imgFilter)
        getListFilter()
        initEvent()
        return root
    }

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

    private fun getListFilter() {
        listFiler.clear()
        listFiler.add("Mã NV")
        listFiler.add("Tên")
        listFiler.add("Email")
        listFiler.add("Tất cả")

        listFilerChosen.clear()
        listFilerChosen.add(0)
    }

    private fun initEvent() {
        imgFilter?.setOnClickListener {
            TimeCardDialog(listFilerChosen, requireContext(), listFiler) {
                if(etSearch?.text.toString().isEmpty()) {
                    adapter.updateList(listTimeCard)
                } else {
                    searchList(etSearch?.text.toString())
                }
            }.show()

        }

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if(etSearch?.text.toString().isEmpty()) {
                    adapter.updateList(listTimeCard)
                } else {
                    searchList(etSearch?.text.toString())
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })

        adapter = TimeCardDateAdapter.TimeCardAdapter(arrayListOf()) {
            DetailTimeCardActivity.start(it, requireActivity())
        }

        rvTimeCard?.adapter = adapter
        cardInsert?.setOnClickListener {
            startActivity(Intent(requireContext(), InsertTimeCardActivity::class.java))
        }

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constants.TIMECARD_NODE)

        // Read from the database

        // Read from the database
        showProgrss()
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                listTimeCard = arrayListOf()
                for(child in dataSnapshot.children) {
                    for(childTimeCard in child.children) {
                        val timeCard: TimeCard?= childTimeCard.getValue(TimeCard::class.java)

                        timeCard?.let {
                            listTimeCard.add(it)
                        }
                    }


                }

                hideProgrss()
                if(!listTimeCard.isNullOrEmpty()) {
                    tvNoData?.visibility = View.GONE
                    rvTimeCard?.visibility = View.VISIBLE
                    adapter.updateList(listTimeCard)
                } else {
                    tvNoData?.visibility = View.VISIBLE
                    rvTimeCard?.visibility = View.GONE
                }

            }

            override fun onCancelled(error: DatabaseError) {
                hideProgrss()
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun searchList(text: String){
        uiScope.launch {
            var listFilter : ArrayList<TimeCard> = arrayListOf()
            if(!listTimeCard.isNullOrEmpty()) {
                var listFilterAll: ArrayList<TimeCard> = arrayListOf()
                val listFilterMaNV: ArrayList<TimeCard> = arrayListOf()
                val listFilterName: ArrayList<TimeCard> = arrayListOf()
                val listFilterEmail: ArrayList<TimeCard> = arrayListOf()
                if(listFilerChosen.contains(3)) {
                    listFilterAll.addAll(listTimeCard.filter { timeCard -> timeCard.user?.maNV.toString().toLowerCase().contains(text.toLowerCase()) ||
                            timeCard.user?.name.toString().toLowerCase().contains(text.toLowerCase()) ||
                            timeCard.user?.email.toString().toLowerCase().contains(text.toLowerCase())})

                    listFilterAll.forEach {
                        if(!listFilter.contains(it)) {
                            listFilter.add(it)
                        }
                    }
                }

                if(listFilerChosen.contains(0)) {
                    listFilterMaNV.addAll(listTimeCard.filter { timeCard -> timeCard.user?.maNV.toString().toLowerCase().contains(text.toLowerCase())})
                    listFilterMaNV.forEach {
                        if(!listFilter.contains(it)) {
                            listFilter.add(it)
                        }
                    }
                }

                if(listFilerChosen.contains(1)) {
                    listFilterName.addAll(listTimeCard.filter { timeCard -> timeCard.user?.name.toString().toLowerCase().contains(text.toLowerCase())})
                    listFilterName.forEach {
                        if(!listFilter.contains(it)) {
                            listFilter.add(it)
                        }
                    }
                }

                if(listFilerChosen.contains(2)) {
                    listFilterEmail.addAll(listTimeCard.filter { timeCard -> timeCard.user?.email.toString().toLowerCase().contains(text.toLowerCase())})
                    listFilterEmail.forEach {
                        if(!listFilter.contains(it)) {
                            listFilter.add(it)
                        }
                    }
                }


            }

            withContext(Dispatchers.Main) {
                adapter.updateList(listFilter)
            }
        }

    }
}