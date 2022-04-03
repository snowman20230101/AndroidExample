package com.windy.libralive.net

import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.windy.libralive.R
import com.windy.libralive.uitl.Utils
import okhttp3.Interceptor
import okhttp3.Response

class NetworkStatusInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        if (!Utils.isConnected()) {
            ToastUtils.showShort(StringUtils.getString(R.string.network_connection_error))
        }
        return chain.proceed(chain.request())
    }
}