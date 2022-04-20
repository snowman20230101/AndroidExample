package com.windy.libralive.kotlin

import com.windy.libralive.base.model.BaseModel

class ViewHolderTest(private val itemView: IBaseTest<BaseModel>) {

    fun bind(model: BaseModel) {
        itemView.bindViewModel(model)
    }
}