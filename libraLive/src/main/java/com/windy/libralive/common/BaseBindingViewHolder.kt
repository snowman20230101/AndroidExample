package com.windy.libralive.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.windy.libralive.base.model.BaseModel
import com.windy.libralive.base.view.IBaseView

class BaseBindingViewHolder(private val baseView: IBaseView<BaseModel>) :
    RecyclerView.ViewHolder(baseView as View) {

    fun bind(viewModel: BaseModel) {
        this.baseView.bindViewModel(viewModel)
    }
}