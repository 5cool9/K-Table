package com.example.k_table

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class Preference3Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_3)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val btnNone = findViewById<TextView>(R.id.btnNone)
        val btnNext = findViewById<AppCompatButton>(R.id.btnNext)

        // 일반 취향 카드들 목록
        val normalCards = listOf(
            findViewById<TextView>(R.id.btnHalal),
            findViewById<TextView>(R.id.btnVegetarian),
            findViewById<TextView>(R.id.btnVegan),
            findViewById<TextView>(R.id.btnMsgFree),
            findViewById<TextView>(R.id.btnGlutenFree),
            findViewById<TextView>(R.id.btnPorkFree),
            findViewById<TextView>(R.id.btnAlcoholFree)
        )

        val allCards = normalCards + btnNone

        //선택 상태를 체크하고 Next 버튼 활성화/비활성화 및 텍스트 색상을 일괄 업데이트하는 함수
        fun updateState() {
            // 일반 카드 중 하나라도 선택되었는지 확인
            val isAnyNormalSelected = normalCards.any { it.isSelected }
            // None이 선택되었는지 확인
            val isNoneSelected = btnNone.isSelected

            btnNext.isEnabled = isAnyNormalSelected || isNoneSelected

            for (card in normalCards) {
                card.setTextColor(if (card.isSelected) Color.WHITE else Color.parseColor("#333333"))
            }
            btnNone.setTextColor(if (btnNone.isSelected) Color.WHITE else Color.parseColor("#008000"))
        }

        // 초기 진입 시 버튼 비활성화 상태 맞추기
        updateState()

        // 일반 취향 카드들 클릭 리스너
        for (card in normalCards) {
            card.setOnClickListener {
                // 일반 카드를 누르면 None은 무조건 선택 해제됨
                btnNone.isSelected = false

                // 현재 카드의 선택 상태 토글
                card.isSelected = !card.isSelected

                // 상태 업데이트 실행
                updateState()
            }
        }

        // None 버튼 클릭 리스너
        btnNone.setOnClickListener {
            // None을 누르면 기존에 선택되어 있던 모든 일반 카드들의 선택을 취소함!
            for (card in normalCards) {
                card.isSelected = false
            }

            // None 자체의 선택 상태 토글
            btnNone.isSelected = !btnNone.isSelected

            // 상태 업데이트 실행
            updateState()
        }

        // 하단 Next 버튼
        btnNext.setOnClickListener {
            val userNickname = intent.getStringExtra("USER_NICKNAME") ?: "USER"
            val userLanguage = intent.getStringExtra("USER_LANGUAGE") ?: "Korean"

            val preferenceKeyMap = mapOf(
                R.id.btnHalal to "HALAL",
                R.id.btnVegetarian to "VEGETARIAN",
                R.id.btnVegan to "VEGAN",
                R.id.btnMsgFree to "MSG_FREE",
                R.id.btnGlutenFree to "GLUTEN_FREE",
                R.id.btnPorkFree to "PORK_FREE",
                R.id.btnAlcoholFree to "ALCOHOL_FREE"
            )

            val selectedPreferences = ArrayList<String>()
            for (card in normalCards) {
                if (card.isSelected) {
                    preferenceKeyMap[card.id]?.let { key ->
                        selectedPreferences.add(key)
                    }
                }
            }
            if (btnNone.isSelected) {
                selectedPreferences.add("None")
            }

            val intent = Intent(this, Preference4Activity::class.java).apply {
                putExtra("USER_NICKNAME", userNickname)
                putExtra("USER_LANGUAGE", userLanguage)
                putStringArrayListExtra("USER_PREFERENCES", selectedPreferences)
            }
            startActivity(intent)
        }
    }
}