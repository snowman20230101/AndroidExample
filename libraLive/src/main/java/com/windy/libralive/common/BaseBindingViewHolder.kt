package com.windy.libralive.common

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.windy.libralive.base.model.BaseModel
import com.windy.libralive.base.view.BaseView
import com.windy.libralive.base.viewmodel.BaseViewModel
import com.windy.libralive.base.view.IBaseView

class BaseBindingViewHolder(private val itemVIew: BaseView<ViewDataBinding, BaseModel>) :
    RecyclerView.ViewHolder(itemVIew) {

    fun bind(viewModel: BaseModel) {
        itemVIew.bindViewModel(viewModel)
    }
}