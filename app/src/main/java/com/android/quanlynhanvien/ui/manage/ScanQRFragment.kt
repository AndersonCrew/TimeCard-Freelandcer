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
                        val cal = Calendar.getInstance()
                        val database = FirebaseDatabase.getInstance()
                        val currentTime = System.currentTimeMillis()
                        val myRef = database.getReference(Constants.TIMECARD_NODE).child(user.maNV?: "-").child(currentTime.toString())
                        val timeCard = TimeCard(user, currentTime)
                        myRef.setValue(timeCard).addOnCompleteListener {
                            if(it.isSuccessful) {
                                Toast.makeText(requireContext(), "Chấm công thành công!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(requireContext(), it.exception?.message?: "Chấm công thất bại", Toast.LENGTH_LONG).show()
                            }
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), it.message?: "Chấm công thất bại", Toast.LENGTH_LONG).show()
                        }
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}