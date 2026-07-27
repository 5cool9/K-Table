package com.example.k_table.api

import com.example.k_table.model.GooglePlaceSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface GooglePlacesApiService {

    // 식당 이름으로 Google Place 검색
    @GET("maps/api/place/textsearch/json")
    suspend fun searchPlace(
        @Query("query") query: String,
        @Query("key") apiKey: String
    ): GooglePlaceSearchResponse


}