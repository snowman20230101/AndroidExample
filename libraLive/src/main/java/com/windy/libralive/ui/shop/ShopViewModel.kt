package com.windy.libralive.ui.shop

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ToastUtils
import com.windy.libralive.base.viewmodel.BaseViewModel
import com.windy.libralive.common.BaseResult
import com.windy.libralive.common.fold
import com.windy.libralive.data.model.home.UserBean
import com.windy.libralive.net.RetrofitFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShopViewModel : BaseViewModel() {
    val text: LiveData<String?> = MutableLiveData<String?>().apply {
        value = "this is shop Fragment"
    }

    init {
        modelClassName = ShopViewModel::class.java.simpleName
    }

    fun login(username: String, password: String) = launchUI {
        val baseResponse = withContext(Dispatchers.IO) {
            val loginJson = RetrofitFactory.getInstance().service.login(username, password)
            if (loginJson.errCode == 0) BaseResult.success(loginJson.data) else BaseResult.failure(
                Throwable("Failed to login ${loginJson.errMsg}")
            )
        }

        baseResponse.fold({
            Log.d(modelClassName, "login: $it")
        }, {
            Log.d(modelClassName, "login: $it")
            ToastUtils.showShort(it.message)
        })
    }

    fun login(userBean: UserBean) = launchUI {
        val baseResponse = withContext(Dispatchers.IO) {
            val loginJson = RetrofitFactory.getInstance().service.login(userBean)
            if (loginJson.errCode == 0) BaseResult.success(loginJson.data) else BaseResult.failure(
                Throwable("Failed to login ${loginJson.errMsg}")
            )
        }

        baseResponse.fold({
            Log.d(modelClassName, "login: $it")
        }, {
            Log.d(modelClassName, "login: $it")
            ToastUtils.showShort(it.message)
        })
    }
}