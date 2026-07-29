package com.example.k_table.api

import com.example.k_table.model.KakaoResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import com.example.k_table.model.KakaoAddressResponse

interface KakaoApiService {

    @GET("v2/local/search/category.json")
    suspend fun searchRestaurant(
        @Header("Authorization") key: String,

        @Query("category_group_code")
        category: String = "FD6",

        @Query("x")
        longitude: String,

        @Query("y")
        latitude: String,

        @Query("radius")
        radius: Int = 2000,

        @Query("size")
        size: Int = 15,

        @Query("page")
        page: Int = 1

    ): retrofit2.Response<KakaoResponse>

    // 주소/지역 검색용 추가
    @GET("v2/local/search/keyword.json")
    suspend fun searchKeywordRestaurant(

        @Header("Authorization")
        key: String,

        @Query("query")
        keyword: String,

        @Query("category_group_code")
        category: String = "FD6",

        @Query("size")
        size: Int = 15,

        @Query("page")
        page: Int = 1

    ): retrofit2.Response<KakaoResponse>

    @GET("v2/local/search/address.json")
    suspend fun searchAddress(

        @Header("Authorization")
        key: String,

        @Query("query")
        keyword: String

    ): retrofit2.Response<KakaoAddressResponse>
}