package com.windy.libralive.kotlin

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.windy.libralive.data.model.home.HomeBean

class ItemOneTestVIew(override val context: Context) : BaseTest<ViewDataBinding, HomeBean>(context){
    override fun bind(model: HomeBean) {

    }

    override fun getLayoutId(): Int {
        TODO("Not yet implemented")
    }
}