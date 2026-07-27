package com.example.k_table.model


data class GooglePlaceSearchResponse(
    val results: List<GooglePlace>
)


data class GooglePlace(
    val place_id: String,
    val photos: List<GooglePhoto>?
)


data class GooglePhoto(
    val photo_reference: String
)