package com.android.quanlynhanvien.ui.manage

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.quanlynhanvien.Constants
import com.android.quanlynhanvien.R
import com.android.quanlynhanvien.model.TimeCard
import com.android.quanlynhanvien.model.TimeCardDate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TimeCardDateAdapter(private val listTimeCardDate: ArrayList<TimeCardDate>, private val itemClick: (timeCard: TimeCard) -> Unit): RecyclerView.Adapter<TimeCardDateAdapter.TimeCardDateViewHolder>() {

    class TimeCardDateViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var tvTime: TextView?= null
        private var rvTimeCard: RecyclerView?= null

        init {
            tvTime = itemView.findViewById(R.id.tvDate)
            rvTimeCard = itemView.findViewById(R.id.rvTimeCard)
        }

        fun bind(timeCardDate: TimeCardDate, itemClick: (timeCard: TimeCard) -> Unit) {
            tvTime?.text = timeCardDate.time
            rvTimeCard?.adapter = TimeCardAdapter(timeCardDate.list, itemClick)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeCardDateViewHolder {
        return TimeCardDateViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_timecard_date, parent, false))
    }

    override fun getItemCount(): Int {
        return listTimeCardDate.size
    }

    override fun onBindViewHolder(holder: TimeCardDateViewHolder, position: Int) {
        if(!listTimeCardDate.isNullOrEmpty()) {
            holder.bind(listTimeCardDate[position], itemClick)
        }
    }

    class TimeCardAdapter(private var listTimeCard: ArrayList<TimeCard>, private val itemClick: (timeCard: TimeCard) -> Unit): RecyclerView.Adapter<TimeCardViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeCardViewHolder {
            return TimeCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_check, parent, false))
        }

        override fun getItemCount(): Int {
            return listTimeCard.size
        }

        override fun onBindViewHolder(holder: TimeCardViewHolder, position: Int) {
            if(!listTimeCard.isNullOrEmpty()) {
                holder.bind(listTimeCard[position])

                holder.itemView.setOnClickListener {
                    itemClick.invoke(listTimeCard[position])
                }
            }
        }

        fun updateList(list: ArrayList<TimeCard>) {
            listTimeCard.clear()
            listTimeCard.addAll(list)
            notifyDataSetChanged()
        }

    }

    class TimeCardViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var tvTimeCheck: TextView?= null
        private var tvName: TextView?= null
        private var tvEmail: TextView?= null

        init {
            tvTimeCheck = itemView.findViewById(R.id.tvTimeCheck)
            tvName = itemView.findViewById(R.id.tvName)
            tvEmail = itemView.findViewById(R.id.tvEmail)
        }

        @SuppressLint("SimpleDateFormat")
        fun bind(timeCard: TimeCard) {
            tvName?.text = timeCard.user?.name?: "-"
            tvEmail?.text = timeCard.user?.email?: "-"
            val date = Date()
            date.time = timeCard.timeCheck?: 0
            tvTimeCheck?.text = SimpleDateFormat(Constants.HOUR_TIME_FORMAT).format(date)
        }
    }

    fun updateList(list: ArrayList<TimeCardDate>) {
        listTimeCardDate.clear()
        listTimeCardDate.addAll(list)
        notifyDataSetChanged()
    }
}