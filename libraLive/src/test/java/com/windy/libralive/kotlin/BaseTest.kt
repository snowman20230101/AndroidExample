package com.windy.libralive.kotlin

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.windy.libralive.base.model.BaseModel

abstract class BaseTest<VB : ViewDataBinding, VM : BaseModel>(open val context: Context) :
    IBaseTest<VM> {

    protected lateinit var binding: VB
    protected lateinit var model: VM

    init {
        initBinding()
    }

    private fun initBinding() {
        val systemService = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DataBindingUtil.inflate<VB>(systemService, getLayoutId(), null, false)
    }

    override fun bindViewModel(model: VM) {
        this.model = model
        bind(model)
        binding.executePendingBindings()
    }

    protected abstract fun bind(model: VM)
    protected abstract fun getLayoutId(): Int
}