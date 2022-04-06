package com.windy.libralive.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.windy.libralive.base.model.BaseModel
import com.windy.libralive.base.viewmodel.BaseViewModel
import com.windy.libralive.data.model.home.HomeBean

abstract class BaseView<VB : ViewDataBinding, VM : BaseModel> constructor(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int,
) : LinearLayout(context, attributeSet, defStyleAttr), IBaseView<VM> {

    protected lateinit var viewModel: VM
    protected lateinit var binding: VB

    init {
        initBinding()
    }

    constructor(context: Context, attributeSet: AttributeSet) : this(context, attributeSet, 0)

    constructor(context: Context) : this(context, null, 0)

    private fun initBinding() {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DataBindingUtil.inflate<VB>(layoutInflater, getLayoutId(), this@BaseView, false)
        binding.root.setOnClickListener {
            onRootClick(it)
        }
        addView(binding.root)
    }

    override fun bindViewModel(viewModel: VM) {
        this@BaseView.viewModel = viewModel
        bind(viewModel)
        binding.executePendingBindings()
    }

    /**
     * 手动绑定，ViewModel
     */
    protected abstract fun bind(viewModel: VM)

    /**
     * 具体显示对应的layout
     */
    protected abstract fun getLayoutId(): Int

    /**
     * 点击事件
     */
    protected open fun onRootClick(view: View) {

    }
}