package com.example.k_table.model

data class KakaoResponse(
    val documents: List<KakaoPlace>
)

data class KakaoPlace(
    val place_name: String,
    val category_name: String,
    val road_address_name: String,
    val address_name: String,
    val phone: String,
    val distance: String
)