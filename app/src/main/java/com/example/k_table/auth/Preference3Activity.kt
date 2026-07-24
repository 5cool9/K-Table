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

        // 뒤로 가기 버튼
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // 취향 선택 카드들 (클릭 시 토글 기능 적용)
        val prefCards = listOf(
            findViewById<TextView>(R.id.btnHalal),
            findViewById<TextView>(R.id.btnVegetarian),
            findViewById<TextView>(R.id.btnVegan),
            findViewById<TextView>(R.id.btnMsgFree),
            findViewById<TextView>(R.id.btnGlutenFree),
            findViewById<TextView>(R.id.btnPorkFree),
            findViewById<TextView>(R.id.btnAlcoholFree),
            findViewById<TextView>(R.id.btnNone)
        )

        for (card in prefCards) {
            card.setOnClickListener {
                // 선택 상태를 반전
                card.isSelected = !card.isSelected

                if (card.id == R.id.btnNone) {
                    card.setTextColor(if (card.isSelected) Color.WHITE else Color.parseColor("#008000"))
                } else {
                    card.setTextColor(if (card.isSelected) Color.WHITE else Color.parseColor("#333333"))
                }
            }
        }

        // 하단 Next 버튼
        val btnNext = findViewById<AppCompatButton>(R.id.btnNext)
        btnNext.setOnClickListener {
            // TODO: 선택된 취향 데이터들을 모아서 다음 단계로 전달하거나 저장하는 로직 작성

            val intent = Intent(this, Preference4Activity::class.java)
            startActivity(intent)
        }
    }
}