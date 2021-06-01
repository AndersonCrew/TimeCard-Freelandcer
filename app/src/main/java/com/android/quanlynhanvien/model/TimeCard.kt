package com.android.quanlynhanvien.model

import java.io.Serializable

class TimeCard(
    var user: User?= null,
    var timeCheck: Long?= null,
    var typeCheck : Int?= null,
    var sanluong : Int?= null
): Serializable