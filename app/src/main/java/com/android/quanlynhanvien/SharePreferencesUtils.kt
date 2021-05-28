package com.android.quanlynhanvien

import android.content.Context
import com.android.quanlynhanvien.model.User
import com.google.gson.Gson
import java.lang.Exception

class SharePreferencesUtils constructor(context: Context) {
    private var mPref =
        context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)

    fun logout() {
        mPref.edit().remove(Constants.ACCESS_TOKEN).apply()
    }

    /**Get Set Boolean value*/
    fun setBoolean(key: String, value: Boolean) {
        mPref?.edit()?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(key: String, valueDefault: Boolean): Boolean {
        return mPref.getBoolean(key, valueDefault)
    }

    /**Get Set Int value*/
    fun setInt(key: String, value: Int) {
        mPref?.edit()?.putInt(key, value)?.apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return mPref?.getInt(key, defaultValue) ?: defaultValue
    }

    /**Get Set String value*/
    fun setString(key: String, value: String) {
        mPref?.edit()?.putString(key, value)?.apply()
    }


    fun getString(key: String, defaultValue: String): String {
        return mPref?.getString(key, defaultValue) ?: defaultValue
    }

    fun saveUser(user: User?) {
        val prefsEditor = mPref.edit()
        val gson = Gson()

        val json = if(user != null) gson.toJson(user) else ""
        prefsEditor.putString(Constants.USER, json)
        prefsEditor.apply()
    }

    fun getUser(): User? {
        try {
            val gson = Gson()
            val json = mPref.getString(Constants.USER, "")
            return gson.fromJson(json, User::class.java)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }
}