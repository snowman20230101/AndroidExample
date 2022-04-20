package com.windy.libralive.kotlin

import com.windy.libralive.base.model.BaseModel

interface IBaseTest<VM : BaseModel> {
    fun bindViewModel(model: VM)
}