package com.example.k_table.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
enum class SuitabilityStatus(val label: String) : Parcelable {
    SUITABLE("적합"),
    CAUTION("주의"),
    UNSUITABLE("부적합")
}

@Parcelize
data class MenuScanResult(
    val id: String = "",
    val koreanName: String,
    val englishName: String,
    val price: String? = null,
    val status: SuitabilityStatus,
    val questionKo: String? = null,
    val isExpanded: Boolean = false
) : Parcelable