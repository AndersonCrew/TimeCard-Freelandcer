package com.android.quanlynhanvien

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.quanlynhanvien.databinding.ItemReportBinding
import com.android.quanlynhanvien.model.SanLuong
import java.text.SimpleDateFormat

class SanLuongAdapter (private val list: ArrayList<SanLuong>): RecyclerView.Adapter<SanLuongAdapter.SanLuongViewHolder>(){
    class SanLuongViewHolder(private val binding: ItemReportBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(sanLuong: SanLuong) {
            binding.tvTime.text = SimpleDateFormat("dd/MM/yyyy").format(sanLuong.time)
            binding.tvSanLuong.text = sanLuong.sanluong.toString() + "KG"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SanLuongViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context))
        return SanLuongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SanLuongViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(listUpdate: ArrayList<SanLuong>) {
        list.clear()
        list.addAll(listUpdate)
        notifyDataSetChanged()
    }

    fun addItem(sanLuong: SanLuong) {
        if(!list.contains(sanLuong)) {
            list.add(sanLuong)
            notifyDataSetChanged()
        }

    }
}