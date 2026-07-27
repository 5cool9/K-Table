package com.example.k_table.model

data class Restaurant(
    val name: String,
    val address: String,
    val feature: String,
    val tags: List<String> = emptyList(),
    var imageUrl: String? = null
)