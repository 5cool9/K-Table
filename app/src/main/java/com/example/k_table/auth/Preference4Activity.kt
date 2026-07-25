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
import com.google.firebase.firestore.FirebaseFirestore

class Preference4Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_4)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val cardOther = findViewById<LinearLayout>(R.id.cardOther)
        val btnNone = findViewById<TextView>(R.id.btnNone)
        val btnComplete = findViewById<AppCompatButton>(R.id.btnComplete)

        // 세부 알레르기 카드 목록
        val detailedCards = listOf(
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

        // 앱이 처음 켜졌을 때 세부 카드들은 숨겨두기
        for (detailCard in detailedCards) {
            detailCard.visibility = View.GONE
        }

        // 기본 카드 목록
        val defaultCards = listOf(
            findViewById<LinearLayout>(R.id.cardPeanuts),
            findViewById<LinearLayout>(R.id.cardTreeNuts),
            findViewById<LinearLayout>(R.id.cardMilk),
            findViewById<LinearLayout>(R.id.cardEggs),
            findViewById<LinearLayout>(R.id.cardSeafood),
            findViewById<LinearLayout>(R.id.cardWheat),
            findViewById<LinearLayout>(R.id.cardSoy),
            findViewById<LinearLayout>(R.id.cardSesame)
        )

        val allAllergyCards = defaultCards + detailedCards

        fun updateState() {
            val isAnyAllergySelected = allAllergyCards.any { it.isSelected }
            val isNoneSelected = btnNone.isSelected

            btnComplete.isEnabled = isAnyAllergySelected || isNoneSelected

            // None 버튼 텍스트 색상 처리
            btnNone.setTextColor(if (btnNone.isSelected) Color.WHITE else Color.parseColor("#008000"))
        }

        // 초기 진입 시 버튼 비활성화 상태 맞추기
        updateState()

        // 기본 카드들 클릭 리스너
        for (card in defaultCards) {
            card.setOnClickListener {
                // 알레르기 카드를 누르면 None은 무조건 선택 해제
                btnNone.isSelected = false
                card.isSelected = !card.isSelected
                updateState()
            }
        }

        // 세부 카드들 클릭 리스너
        for (card in detailedCards) {
            card.setOnClickListener {
                // 세부 카드를 누르면 None은 무조건 선택 해제
                btnNone.isSelected = false
                card.isSelected = !card.isSelected
                updateState()
            }
        }

        // Other 버튼 클릭 리스너
        cardOther.setOnClickListener {
            val gridDefaultAllergy = findViewById<GridLayout>(R.id.gridDefaultAllergy)

            gridDefaultAllergy.removeView(cardOther)

            for (detailCard in detailedCards) {
                gridDefaultAllergy.removeView(detailCard)
            }

            for (detailCard in detailedCards) {
                detailCard.visibility = View.VISIBLE
                gridDefaultAllergy.addView(detailCard)
            }
        }


        btnNone.setOnClickListener {

            for (card in allAllergyCards) {
                card.isSelected = false
            }

            btnNone.isSelected = !btnNone.isSelected
            updateState()
        }

        // 완료 버튼
        btnComplete.setOnClickListener {
            val userNickname = intent.getStringExtra("USER_NICKNAME") ?: "USER"
            val userLanguage = intent.getStringExtra("USER_LANGUAGE") ?: "Korean"
            val userPreferences = intent.getStringArrayListExtra("USER_PREFERENCES") ?: arrayListOf()

            val selectedAllergies = mutableListOf<String>()

            val allergyKeyMap = mapOf(
                R.id.cardPeanuts to "PEANUTS",
                R.id.cardTreeNuts to "TREE_NUTS",
                R.id.cardMilk to "MILK",
                R.id.cardEggs to "EGGS",
                R.id.cardSeafood to "SEAFOOD",
                R.id.cardWheat to "WHEAT",
                R.id.cardSoy to "SOY",
                R.id.cardSesame to "SESAME",
                R.id.cardCorn to "CORN",
                R.id.cardChicken to "CHICKEN",
                R.id.cardBeef to "BEEF",
                R.id.cardPork to "PORK",
                R.id.cardTomato to "TOMATO",
                R.id.cardMushroom to "MUSHROOM",
                R.id.cardCoconut to "COCONUT",
                R.id.cardKiwi to "KIWI",
                R.id.cardPeach to "PEACH",
                R.id.cardBanana to "BANANA",
                R.id.cardGarlic to "GARLIC",
                R.id.cardOnion to "ONION",
                R.id.cardSpices to "SPICES"
            )

            for (card in allAllergyCards) {
                if (card.isSelected) {
                    // ID에 매칭되는 고유 키값을 가져와서 리스트에 추가
                    allergyKeyMap[card.id]?.let { key ->
                        selectedAllergies.add(key)
                    }
                }
            }

            if (btnNone.isSelected) {
                selectedAllergies.add("NONE")
            }

            val userInfo = UserPreference(
                nickname = userNickname,
                language = userLanguage,
                preferences = userPreferences,
                allergies = selectedAllergies
            )

            val currentUserUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: userNickname

            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUserUid)
                .set(userInfo)
                .addOnSuccessListener {
                    // 저장 성공 시 홈 화면으로 이동
                    val nextIntent = Intent(this, WelcomeActivity::class.java).apply {
                        putExtra("USER_NICKNAME", userNickname)
                    }
                    startActivity(nextIntent)
                    finish()
                }
                .addOnFailureListener { e ->
                    // 실패 시 처리
                }


        }
    }
}