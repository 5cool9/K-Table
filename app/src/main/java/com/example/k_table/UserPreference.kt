package com.example.k_table

data class UserPreference(
    val nickname: String = "",
    val language: String = "",
    val preferences: List<String> = listOf(), // 3단계
    val allergies: List<String> = listOf()       // 4단계
)