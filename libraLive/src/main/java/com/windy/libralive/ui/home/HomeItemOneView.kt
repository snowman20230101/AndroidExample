package com.windy.libralive.ui.home

import android.content.Context
import com.windy.libralive.R
import com.windy.libralive.base.model.BaseModel
import com.windy.libralive.base.viewmodel.BaseViewModel
import com.windy.libralive.base.view.BaseView
import com.windy.libralive.base.view.IBaseView
import com.windy.libralive.data.model.home.HomeBean
import com.windy.libralive.databinding.ItemHomeOneBinding

class HomeItemOneView(context: Context) :
    BaseView<ItemHomeOneBinding, HomeBean>(context) {

    override fun bind(viewModel: HomeBean) {
        binding.vm = viewModel
    }

    override fun getLayoutId(): Int = R.layout.item_home_one

}