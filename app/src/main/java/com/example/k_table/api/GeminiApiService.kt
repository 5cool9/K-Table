package com.example.k_table.api

import GeminiVisionRequest
import GeminiVisionResponse
import com.example.k_table.model.GeminiRequest
import com.example.k_table.model.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {

    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generate(

        @Query("key")
        apiKey: String,

        @Body
        request: GeminiRequest

    ): Response<GeminiResponse>

    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateVision(
        @Query("key") apiKey: String,
        @Body request: GeminiVisionRequest
    ): Response<GeminiVisionResponse>

}