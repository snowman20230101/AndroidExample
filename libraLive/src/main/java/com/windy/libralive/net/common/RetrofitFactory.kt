package com.windy.libralive.net.common

import com.windy.libralive.net.api.Api
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class RetrofitFactory private constructor(private val hostType: Int = ANDROID_URL) :
    BaseRetrofitFactory() {

    val service by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        getService(Api::class.java, hostType)
    }

    // 单利
    companion object {
        // 第一种
//        val instance: RetrofitFactory by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
//            RetrofitFactory(hostType = ANDROID_URL)
//        }

        // 第二种
        private var instance: RetrofitFactory? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: RetrofitFactory(hostType = ANDROID_URL).also { it ->
                instance = it
            }
        }
    }

    override fun handleBuilder(builder: OkHttpClient.Builder) {

    }

    override fun retrofitBuilder(builder: Retrofit.Builder) {

    }
}