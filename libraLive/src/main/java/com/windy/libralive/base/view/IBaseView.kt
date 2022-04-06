package com.windy.libralive.base.view

import com.windy.libralive.base.model.BaseModel

interface IBaseView<VM : BaseModel> {
    fun bindViewModel(viewModel: VM)
}