package com.windy.libralive.model

import java.io.Serializable

data class BaseResponse<out T> constructor(val errorMsg: String, val code: Int, val data: T) :
    Serializable