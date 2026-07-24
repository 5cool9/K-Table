package com.example.k_table

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class Preference4Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_4)

        // 뒤로 가기
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // 요소 연결
        val cardOther = findViewById<LinearLayout>(R.id.cardOther)
        val gridOtherAllergy = findViewById<GridLayout>(R.id.gridOtherAllergy)
        val btnNone = findViewById<TextView>(R.id.btnNone)
        val btnComplete = findViewById<AppCompatButton>(R.id.btnComplete)

        // 모든 알레르기 카드들에 선택(토글) 및 확장 효과 적용하기
        val allCards = listOf(
            findViewById<LinearLayout>(R.id.cardPeanuts),
            findViewById<LinearLayout>(R.id.cardTreeNuts),
            findViewById<LinearLayout>(R.id.cardMilk),
            findViewById<LinearLayout>(R.id.cardEggs),
            findViewById<LinearLayout>(R.id.cardSeafood),
            findViewById<LinearLayout>(R.id.cardWheat),
            findViewById<LinearLayout>(R.id.cardSoy),
            findViewById<LinearLayout>(R.id.cardSesame),
            cardOther,
            findViewById<LinearLayout>(R.id.cardCorn),
            findViewById<LinearLayout>(R.id.cardChicken),
            findViewById<LinearLayout>(R.id.cardBeef),
            findViewById<LinearLayout>(R.id.cardPork),
            findViewById<LinearLayout>(R.id.cardTomato),
            findViewById<LinearLayout>(R.id.cardMushroom),
            findViewById<LinearLayout>(R.id.cardCoconut),
            findViewById<LinearLayout>(R.id.cardKiwi),
            findViewById<LinearLayout>(R.id.cardPeach),
            findViewById<LinearLayout>(R.id.cardBanana),
            findViewById<LinearLayout>(R.id.cardGarlic),
            findViewById<LinearLayout>(R.id.cardOnion),
            findViewById<LinearLayout>(R.id.cardSpices)
        )

        for (card in allCards) {
            card.setOnClickListener {
                if (card.id == R.id.cardOther) {
                    // 1. Other 버튼 자체를 화면에서 숨김 처리
                    card.visibility = View.GONE

                    // 2. 숨겨져 있던 세부 알레르기 그리드를 나타냄
                    gridOtherAllergy.visibility = View.VISIBLE
                } else {
                    card.isSelected = !card.isSelected
                }
            }
        }

        // None 버튼 토글 효과
        btnNone.setOnClickListener {
            btnNone.isSelected = !btnNone.isSelected
            btnNone.setTextColor(if (btnNone.isSelected) Color.WHITE else Color.parseColor("#008000"))
        }

        // 버튼 클릭 시 환영 페이지로 이동
        btnComplete.setOnClickListener {
            val userNickname = intent.getStringExtra("USER_NICKNAME") ?: "USER"

            val intent = Intent(this, WelcomeActivity::class.java).apply {
                putExtra("USER_NICKNAME", userNickname)
            }
            startActivity(intent)
        }
    }
}