package com.windy.libralive.kotlin

import android.content.Context
import com.windy.libralive.base.model.BaseModel

class TestMain {
    fun test() {
        val context: Context? = null
        ViewHolderTest(ItemOneTestVIew(null!!) as IBaseTest<BaseModel>)
    }
}