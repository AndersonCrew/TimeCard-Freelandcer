package com.android.quanlynhanvien

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.android.quanlynhanvien.model.User


class SpinnerStaffAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val list: List<String>):
    ArrayAdapter<Any>(context, layoutResource, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(position, convertView, parent)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(layoutResource, parent, false) as TextView
        view.text = list[position]
        return view
    }
}