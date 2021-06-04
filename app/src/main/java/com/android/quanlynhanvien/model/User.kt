package com.android.quanlynhanvien.model

import java.io.Serializable

data class User(
    var isAdmin: Boolean = false,
    var gioitinh: Int = 0,
    var cmnd: Int?= null,
    var dantoc: String?= null,
    var address: String?= null,
    var name: String? = null,
    var birthDay: String? = null,
    var email: String? = null,
    var maNV: String? = null,
    var avatar: String? = null,
    var phoneNumber: String? = null,
    var password: String?= null
): Serializable {
    var urlQRCode: String? = null
}