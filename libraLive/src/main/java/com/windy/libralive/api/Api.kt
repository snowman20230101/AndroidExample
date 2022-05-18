package com.windy.libralive.api

import com.windy.libralive.data.model.home.BannerBean
import com.windy.libralive.data.model.BaseListResponse
import com.windy.libralive.data.model.BaseResponse
import com.windy.libralive.data.model.home.HomeBean
import com.windy.libralive.data.model.home.LoginBean
import com.windy.libralive.data.model.home.UserBean
import retrofit2.http.*

interface Api {
    // https://www.wanandroid.com/banner/json

    /**
     * 首页banner
     */
    @GET(HttpsApi.banner)
    suspend fun getBannerJson(): BaseResponse<List<BannerBean>>

    /**
     * 首页文章列表
     */
    @GET(HttpsApi.article_list_json + "{page}/json")
    suspend fun getArticleJson(@Path("page") page: Int): BaseResponse<BaseListResponse<MutableList<HomeBean>>>


    @FormUrlEncoded
    @POST(HttpsApi.login)
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): BaseResponse<LoginBean>


    @POST(HttpsApi.login)
    suspend fun login(@Body userBean: UserBean): BaseResponse<LoginBean>
}