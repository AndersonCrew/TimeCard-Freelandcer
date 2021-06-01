package com.android.quanlynhanvien.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.quanlynhanvien.databinding.ItemCheckBinding
import com.android.quanlynhanvien.model.TimeCard
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TimeCardAdapter(private val list: ArrayList<TimeCard>,private val listener: (timeCard: TimeCard) -> Unit): RecyclerView.Adapter<TimeCardAdapter.TimeCardViewHolder>() {

    class TimeCardViewHolder(private val binding: ItemCheckBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(timeCard: TimeCard) {
            binding.tvName.text = timeCard.user?.name?: "-"
            binding.tvEmail.text = timeCard.user?.email?: "-"

            val date = Date(timeCard.timeCheck?: System.currentTimeMillis())
            val timeCheck = SimpleDateFormat("hh:mm dd/MM/yyyy").format(date)
            binding.tvTimeCheck.text = timeCheck

            if(timeCard.typeCheck == 1) {
                binding.tvSanluong.visibility = View.VISIBLE
                binding.tvSanluong.text = "Sản lượng : ${timeCard.sanluong}Kg"
            } else {
                binding.tvSanluong.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeCardViewHolder {
        val binding = ItemCheckBinding.inflate(LayoutInflater.from(parent.context))
        return TimeCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeCardViewHolder, position: Int) {
        holder.bind(list[position])
        holder.itemView.setOnClickListener { listener.invoke(list[position]) }
    }

    override fun getItemCount(): Int {
       return list.size
    }

    fun addItem(timeCard: TimeCard) {
        if(!list.contains(timeCard)) {
            list.add(timeCard)
            notifyItemChanged(list.size)
        }
    }

    fun updateList(listTimeCard: ArrayList<TimeCard>) {
        list.clear()
        list.addAll(listTimeCard)
        notifyDataSetChanged()
    }
}