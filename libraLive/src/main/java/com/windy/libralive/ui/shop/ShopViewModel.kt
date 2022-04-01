package com.windy.libralive.ui.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.windy.libralive.base.BaseViewModel

class ShopViewModel : BaseViewModel() {
    val text: LiveData<String?> = MutableLiveData<String?>().apply {
        value = "this is shop Fragment"
    }

    init {
        modelClassName = ShopViewModel::class.java.simpleName
    }
}