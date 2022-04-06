package com.windy.libralive.data.model

import java.io.Serializable

data class BaseResponse<out T> constructor(
    val errMsg: String,
    val errCode: Int,
    val data: T
) : Serializable