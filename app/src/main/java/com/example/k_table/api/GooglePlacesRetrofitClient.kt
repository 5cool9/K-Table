package com.example.k_table.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object GooglePlacesRetrofitClient {

    private const val BASE_URL = "https://maps.googleapis.com/"

    val api: GooglePlacesApiService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GooglePlacesApiService::class.java)
    }
}