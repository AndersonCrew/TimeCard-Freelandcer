package com.android.quanlynhanvien.ui.manage

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnCreateContextMenuListener
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.quanlynhanvien.Constants
import com.android.quanlynhanvien.PermissionUtil
import com.android.quanlynhanvien.R
import com.android.quanlynhanvien.SharePreferencesUtils
import com.android.quanlynhanvien.model.User
import com.android.quanlynhanvien.ui.InsertStaffActivity
import com.android.quanlynhanvien.ui.detail.DetailStaffActivity
import com.android.quanlynhanvien.ui.dialog.ShowQrDialog
import com.android.quanlynhanvien.ui.dialog.TimeCardDialog
import com.android.quanlynhanvien.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ManageStaffFragment : Fragment(), OnCreateContextMenuListener {

    private var cardInsert: CardView?= null
    private var rvStaff: RecyclerView?= null
    private var llTitle: LinearLayout?= null
    private var tvNoData: TextView?= null
    private var imgFilter: ImageView?= null
    private var btnLogout: ImageView?= null
    private lateinit var adapter: StaffAdapter
    private var dialog: ShowQrDialog?= null
    private var etSearch: EditText?= null
    private val EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 123
    private var progressDialog: ProgressDialog?= null
    private var listFiler : ArrayList<String> = arrayListOf()
    private var listFilerChosen : ArrayList<Int> = arrayListOf()
    val uiScope = CoroutineScope(Dispatchers.IO)
    private var listStaff: ArrayList<User> = arrayListOf()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_manage_staff, container, false)
        cardInsert = root.findViewById(R.id.imgInsert)
        rvStaff = root.findViewById(R.id.rvStaff)
        tvNoData = root.findViewById(R.id.tvNodata)
        imgFilter = root.findViewById(R.id.imgFilter)
        etSearch = root.findViewById(R.id.etSearch)
        btnLogout = root.findViewById(R.id.btnLogout)

        imgFilter?.setOnCreateContextMenuListener(this)
        registerForContextMenu(imgFilter!!)
        initEvent()
        getListFilter()
        return root
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
        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if(etSearch?.text.toString().isEmpty()) {
                    adapter.updateList(listStaff)
                } else {
                    searchList(etSearch?.text.toString())
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setCancelable(false)
        progressDialog?.setMessage("Loading...")


        imgFilter?.setOnClickListener {
            TimeCardDialog(listFilerChosen, requireContext(), listFiler) {
                if(etSearch?.text.toString().isEmpty()) {
                    adapter.updateList(listStaff)
                } else {
                    searchList(etSearch?.text.toString())
                }
            }.show()

        }
        adapter = StaffAdapter(arrayListOf()) { user, type ->

            //Item Click
            if(type == 0) {
                DetailStaffActivity.start(user, requireActivity())
            } else {
                //Image QRClick
                dialog = ShowQrDialog(requireContext(), user) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
                }

                dialog?.show()
            }
        }

        rvStaff?.adapter = adapter
        cardInsert?.setOnClickListener {
            startActivity(Intent(requireContext(), InsertStaffActivity::class.java))
        }

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constants.CHILD_NODE_USER)

        // Read from the database

        // Read from the database

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                listStaff = arrayListOf()
                for(child in dataSnapshot.children) {
                    val user: User? = child.getValue(User::class.java)
                    user?.let {
                        listStaff.add(it)
                    }
                }

                if(!listStaff.isNullOrEmpty()) {
                    tvNoData?.visibility = View.GONE
                    llTitle?.visibility = View.VISIBLE
                    rvStaff?.visibility = View.VISIBLE
                    adapter.updateList(listStaff)
                } else {
                    tvNoData?.visibility = View.VISIBLE
                    llTitle?.visibility = View.GONE
                    rvStaff?.visibility = View.GONE
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
            }
        })

        btnLogout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            SharePreferencesUtils(requireContext()).also {
                it.saveUser(null)
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
        }
    }

    private fun searchList(text: String){
        uiScope.launch {
            var listFilter : ArrayList<User> = arrayListOf()
            if(!listStaff.isNullOrEmpty()) {
                var listFilterAll: ArrayList<User> = arrayListOf()
                val listFilterMaNV: ArrayList<User> = arrayListOf()
                val listFilterName: ArrayList<User> = arrayListOf()
                val listFilterEmail: ArrayList<User> = arrayListOf()
                if(listFilerChosen.contains(3)) {
                    listFilterAll.addAll(listStaff.filter { user -> user.maNV.toString().toLowerCase().contains(text.toLowerCase()) ||
                            user.name.toString().toLowerCase().contains(text.toLowerCase()) ||
                            user.email.toString().toLowerCase().contains(text.toLowerCase())})

                    listFilterAll.forEach {
                        if(!listFilter.contains(it)) {
                            listFilter.add(it)
                        }
                    }
                }

                if(listFilerChosen.contains(0)) {
                    listFilterMaNV.addAll(listStaff.filter { user -> user.maNV.toString().toLowerCase().contains(text.toLowerCase())})

                    listFilterMaNV.forEach {
                        if(!listFilter.contains(it)) {
                            listFilter.add(it)
                        }
                    }
                }

                if(listFilerChosen.contains(1)) {
                    listFilterName.addAll(listStaff.filter { user -> user.name.toString().toLowerCase().contains(text.toLowerCase())})
                    listFilterName.forEach {
                        if(!listFilter.contains(it)) {
                            listFilter.add(it)
                        }
                    }
                }

                if(listFilerChosen.contains(2)) {
                    listFilterEmail.addAll(listStaff.filter { user -> user.email.toString().toLowerCase().contains(text.toLowerCase())})
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (PermissionUtil.checkPermissions(requireContext(), PermissionUtil.permissionsStorage)) {
                dialog?.onSaveFile()
            } else {
                Toast.makeText(requireContext(), "External Storage permission needed. Please allow in App Settings for additional functionality", Toast.LENGTH_LONG).show()
            }

        }
    }
}