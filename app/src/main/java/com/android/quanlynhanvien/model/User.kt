package com.android.quanlynhanvien.model

import java.io.Serializable

data class User(
    var name: String? = null,
    var birthDay: String? = null,
    var email: String? = null,
    var maNV: String? = null
): Serializable {
    var urlQRCode: String? = null
}