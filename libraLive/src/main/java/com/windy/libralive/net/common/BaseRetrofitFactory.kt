package com.windy.libralive.net.common

import androidx.viewbinding.BuildConfig
import com.windy.libralive.net.api.HttpApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val ANDROID_URL = 1
const val WAN_ANDROID = 2

/**
 * 对网络请求，Okhttp 和 Retrofit 的顶层封装，便于扩展
 */
abstract class BaseRetrofitFactory {
    /**
     * 对于Okhttp 功的扩展
     */
    protected abstract fun handleBuilder(builder: OkHttpClient.Builder)

    /**
     * 对于 Retrofit 功能扩展
     */
    protected abstract fun retrofitBuilder(builder: Retrofit.Builder)

    // java 中的静态区域
    companion object {
        private const val DEFAULT_TIME = 20
    }

    private val okHttpClient: OkHttpClient
        get() {
            val okHttpClientbuilder = OkHttpClient.Builder()

            okHttpClientbuilder.connectTimeout(DEFAULT_TIME.toLong(), TimeUnit.SECONDS)
            okHttpClientbuilder.readTimeout(DEFAULT_TIME.toLong(), TimeUnit.SECONDS)
            okHttpClientbuilder.writeTimeout(DEFAULT_TIME.toLong(), TimeUnit.SECONDS)
            okHttpClientbuilder.retryOnConnectionFailure(true) // 重定向

            // 日志拦截器
            okHttpClientbuilder.addInterceptor(HttpLoggingInterceptor().apply {
                this.level =
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
            })
            okHttpClientbuilder.addInterceptor(HeadersInterceptor()) // 请求头拦截器

            handleBuilder(okHttpClientbuilder)

            return okHttpClientbuilder.build()
        }

    /**
     * 创建Retrofit
     */
    fun <T> getService(serviceClass: Class<T>, hostHype: Int): T {
        val retrofitBuilder = Retrofit.Builder()
        retrofitBuilder.client(okHttpClient)
        retrofitBuilder.baseUrl(getHost(hostHype))

        // response 转换器
        retrofitBuilder.addConverterFactory(GsonConverterFactory.create())

        // 请求适配器
        retrofitBuilder.addCallAdapterFactory(RxJava3CallAdapterFactory.create())

        retrofitBuilder(retrofitBuilder)

        return create(serviceClass, retrofitBuilder.build())
    }

    private fun <T> create(service: Class<T>, retrofit: Retrofit): T {
        return retrofit.create(service)
    }

    /**
     * 转 HTTP Host URL
     */
    private fun getHost(hostType: Int): String {
        return when (hostType) {
            ANDROID_URL -> HttpApi.BASE_URL
            WAN_ANDROID -> HttpApi.GANK_IO_URL
            else -> HttpApi.BASE_URL
        }
    }
}