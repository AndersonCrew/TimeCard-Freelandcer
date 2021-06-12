package com.android.quanlynhanvien.ui.manage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.quanlynhanvien.Constants
import com.android.quanlynhanvien.R
import com.android.quanlynhanvien.SharePreferencesUtils
import com.android.quanlynhanvien.model.TimeCard
import com.android.quanlynhanvien.model.User
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*


class ScanQRFragment : Fragment() {
    private var imgScan: ImageView?= null
    internal var qrScanIntegrator: IntentIntegrator? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
         val view = inflater.inflate(R.layout.fragment_scan_q_r, container, false)
        imgScan = view.findViewById(R.id.imgScan)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        qrScanIntegrator = IntentIntegrator.forSupportFragment(this)
        qrScanIntegrator?.setOrientationLocked(false)

        // Different Customization option...
        qrScanIntegrator?.setPrompt(getString(R.string.scan_a_code))
        qrScanIntegrator?.setCameraId(0)  // Use a specific camera of the device
        qrScanIntegrator?.setBeepEnabled(false)
        qrScanIntegrator?.setBarcodeImageEnabled(true)

        imgScan?.setOnClickListener {
            qrScanIntegrator?.initiateScan()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            // If QRCode has no data.
            if (result.contents == null) {
                //Toast.makeText(activity, R.string.result_not_found, Toast.LENGTH_LONG).show()
            } else {
                // If QRCode contains data.
                try {
                    // Converting the data to json format
                   val jsonUser = Gson().fromJson(result.contents, User::class.java)
                    jsonUser?.let {user ->
                        checkInStaff(user)
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkInStaff(mUser: User) {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        val timeCard = TimeCard()
        timeCard.apply {
            user = mUser
            timeCheck = System.currentTimeMillis()
            typeCheck = 0
        }

       FirebaseDatabase.getInstance().getReference(Constants.TIMECARD_NODE)
            .child(mUser.maNV ?: "-")
            .child(year.toString())
            .child(month.toString())
            .child(day.toString())
            .child(Constants.CHECK_IN).setValue(timeCard)
            .addOnCompleteListener { update ->
                if (update.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Chấm công thành công!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Vui lòng kiểm tra lại kết nối mạng!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}