package com.android.quanlynhanvien.ui.manage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.quanlynhanvien.R
import com.android.quanlynhanvien.model.User
import com.squareup.picasso.Picasso

class StaffAdapter(private var list: ArrayList<User>,private val itemQRClick: (user: User, typeClick: Int) -> Unit): RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    class StaffViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var tvName: TextView?= null
        private var tvMaNV: TextView?= null
        private var tvBirthday: TextView?= null
        private var tvEmail: TextView?= null
        private var imgQrCode: ImageView?= null

        init {
            tvMaNV = itemView.findViewById(R.id.tvMaNV)
            tvName = itemView.findViewById(R.id.tvName)
            tvBirthday = itemView.findViewById(R.id.tvBirthday)
            tvEmail = itemView.findViewById(R.id.tvEmail)
            imgQrCode = itemView.findViewById(R.id.imgQrCode)
        }

        fun bind(user: User, itemQRClick: (user: User, typeClick: Int) -> Unit) {
            tvMaNV?.text = user.maNV?: "-"
            tvName?.text = user.name?: "-"
            tvBirthday?.text = user.birthDay.toString()
            tvEmail?.text = user.email?: "-"
            user.urlQRCode?.let { url ->
                Picasso.get().load(url).into(imgQrCode)

                imgQrCode?.setOnClickListener {
                    itemQRClick.invoke(user, 1)
                }
            }

            itemView.setOnClickListener {
                itemQRClick.invoke(user, 0)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_staff, parent, false)
        return StaffViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        if(!list.isNullOrEmpty()) {
            holder.bind(list[position], itemQRClick)
        }
    }

    fun updateList(listUpdate: ArrayList<User>) {
        list.clear()
        list.addAll(listUpdate)
        notifyDataSetChanged()
    }
}