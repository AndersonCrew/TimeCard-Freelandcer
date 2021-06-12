package com.android.quanlynhanvien.ui.manage

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.quanlynhanvien.databinding.ItemTimecardDateBinding
import com.android.quanlynhanvien.model.TimeCard
import com.android.quanlynhanvien.model.TimeCardDate
import java.text.SimpleDateFormat

class TimeCardAdapter(private val list: ArrayList<TimeCardDate>, private val listener: (timeCard: TimeCard) -> Unit): RecyclerView.Adapter<TimeCardAdapter.TimeCardViewHolder>() {

    class TimeCardViewHolder(private val binding: ItemTimecardDateBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bind(timeCardDate: TimeCardDate) {
            binding.tvDate.text = SimpleDateFormat("dd/MM/yyyy").format(timeCardDate.time)
            if(timeCardDate.list?.find { it -> it.typeCheck == 0 } != null) {
                binding.tvCheckIn.text = "Time : ${SimpleDateFormat("hh:mm dd/MM/yyyy").format(timeCardDate.list?.find { it.typeCheck == 0 }!!.timeCheck)}"
                binding.tvCheckIn.setTextColor(Color.GRAY)

                binding.tvName.text = timeCardDate.list?.find { it -> it.typeCheck == 0 }?.user?.name
                binding.tvEmail.text = timeCardDate.list?.find { it -> it.typeCheck == 0 }?.user?.email
            } else {
                binding.tvCheckIn.text = "Check In Empty"
                binding.tvCheckIn.setTextColor(Color.RED)
            }

            if(timeCardDate.list?.find { it -> it.typeCheck == 1 } != null) {
                binding.tvCheckOut.text = "Time : ${SimpleDateFormat("hh:mm dd/MM/yyyy").format(timeCardDate.list?.find { it.typeCheck == 1 }!!.timeCheck)}"
                binding.tvCheckOut.setTextColor(Color.GRAY)
                binding.tvSanLuong.visibility = View.VISIBLE
                binding.tvSanLuong.text = "Sản lượng : ${timeCardDate.list?.find { it.typeCheck == 1 }!!.sanluong} Kg"

                binding.tvName.text = timeCardDate.list?.find { it -> it.typeCheck == 1 }?.user?.name
                binding.tvEmail.text = timeCardDate.list?.find { it -> it.typeCheck == 1 }?.user?.email
            } else {
                binding.tvCheckOut.text = "Check Out Empty"
                binding.tvCheckOut.setTextColor(Color.RED)
                binding.tvSanLuong.visibility = View.GONE
            }
         }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeCardViewHolder {
        val binding = ItemTimecardDateBinding.inflate(LayoutInflater.from(parent.context))
        return TimeCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeCardViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(listTimeCard: ArrayList<TimeCardDate>) {
        list.clear()
        list.addAll(listTimeCard)
        notifyDataSetChanged()
    }


}