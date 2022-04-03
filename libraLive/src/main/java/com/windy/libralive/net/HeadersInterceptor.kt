package com.windy.libralive.net

import okhttp3.Interceptor
import okhttp3.Response


/**
 * 请求头拦截器
 */
class HeadersInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request();
        var requestBuidler = request.newBuilder();

        if ("GET" == request.method) {

        } else if ("POST" == request.method) {

        }

        requestBuidler.addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Connection", "keep-alive")
            .addHeader("Accept", "*/*")
            .addHeader("Accept-Encoding", "Vary")

        return chain.proceed(requestBuidler.build())
    }
}