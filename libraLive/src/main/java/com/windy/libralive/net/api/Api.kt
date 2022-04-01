package com.windy.libralive.net.api

import com.windy.libralive.data.model.BannerBean
import com.windy.libralive.data.model.BaseResponse
import retrofit2.http.GET

interface Api {
    // https://www.wanandroid.com/banner/json

    /**
     * 首页banner
     */
    @GET(HttpApi.banner)
    suspend fun getBannerJson(): BaseResponse<List<BannerBean>>

}