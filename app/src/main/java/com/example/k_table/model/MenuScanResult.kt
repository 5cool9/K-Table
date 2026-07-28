package com.example.k_table.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

/**
 * 메뉴 하나의 적합도 상태
 * - SUITABLE   : 적합 (초록)
 * - CAUTION    : 주의 (주황)
 * - UNSUITABLE : 부적합 (빨강)
 */
@Parcelize
enum class SuitabilityStatus(val label: String) : Parcelable {
    SUITABLE("적합"),
    CAUTION("주의"),
    UNSUITABLE("부적합")
}

/**
 * 스캔 결과 카드 하나에 해당하는 데이터
 *
 * Gemini 분석 결과를 받으면 이 데이터 클래스 리스트로 매핑해서
 * ScanResultAdapter.submitList(list) 에 그대로 넣으면 됨
 *
 * @param koreanName   메뉴 한글명 (예: "불고기 덮밥")
 * @param englishName  메뉴 영문명 (예: "Bulgogi Rice Bowl")
 * @param status       적합/주의/부적합 상태
 * @param questionKo   "사장님께 이렇게 물어보세요" 에 들어갈 한국어 질문 텍스트
 * @param isExpanded   현재 카드가 펼쳐진 상태인지
 */

@Parcelize
data class MenuScanResult(
    val id: String = "",
    val koreanName: String,
    val englishName: String,
    val status: SuitabilityStatus,
    val questionKo: String? = null,
    val isExpanded: Boolean = false
) : Parcelable