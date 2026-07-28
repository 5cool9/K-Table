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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Preference4Activity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_4)

        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)

        if (isEditMode) {

            tvTitle.text = "알레르기 설정"
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val cardOther = findViewById<LinearLayout>(R.id.cardOther)
        val btnNone = findViewById<TextView>(R.id.btnNone)
        val btnComplete = findViewById<AppCompatButton>(R.id.btnComplete)

        btnBack.setOnClickListener {

            if (isEditMode) {

                val intent =
                    Intent(this, MainActivity::class.java)

                intent.putExtra(
                    "OPEN_MYPAGE",
                    true
                )
                startActivity(intent)

                finish()

            } else {

                finish()
            }
        }


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


        for (card in detailedCards) {
            card.visibility = View.GONE
        }


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

            val isAnySelected =
                allAllergyCards.any { it.isSelected }

            btnComplete.isEnabled =
                isAnySelected || btnNone.isSelected


            btnNone.setTextColor(
                if (btnNone.isSelected)
                    Color.WHITE
                else
                    Color.parseColor("#008000")
            )
        }



        if (isEditMode) {

            btnComplete.text = "완료"

            loadUserAllergies(
                allAllergyCards,
                btnNone
            )

        } else {

            updateState()
        }



        for (card in defaultCards) {

            card.setOnClickListener {

                btnNone.isSelected = false

                card.isSelected =
                    !card.isSelected

                updateState()
            }
        }



        for (card in detailedCards) {

            card.setOnClickListener {

                btnNone.isSelected = false

                card.isSelected =
                    !card.isSelected

                updateState()
            }
        }



        cardOther.setOnClickListener {

            val gridDefaultAllergy =
                findViewById<GridLayout>(R.id.gridDefaultAllergy)


            if (cardOther.parent != null) {
                gridDefaultAllergy.removeView(cardOther)
            }


            for (detailCard in detailedCards) {
                gridDefaultAllergy.removeView(detailCard)
            }


            for (detailCard in detailedCards) {

                detailCard.visibility =
                    View.VISIBLE

                gridDefaultAllergy.addView(detailCard)
            }
        }



        btnNone.setOnClickListener {

            for (card in allAllergyCards) {
                card.isSelected = false
            }

            btnNone.isSelected =
                !btnNone.isSelected

            updateState()
        }



        btnComplete.setOnClickListener {


            val selectedAllergies =
                mutableListOf<String>()


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

                    allergyKeyMap[card.id]?.let { key ->

                        selectedAllergies.add(key)

                    }
                }
            }


            if (btnNone.isSelected) {

                selectedAllergies.add("NONE")

            }



            if (isEditMode) {


                updateAllergies(
                    selectedAllergies
                )


            } else {


                val userNickname =
                    intent.getStringExtra("USER_NICKNAME")
                        ?: "USER"


                val userLanguage =
                    intent.getStringExtra("USER_LANGUAGE")
                        ?: "Korean"


                val userPreferences =
                    intent.getStringArrayListExtra(
                        "USER_PREFERENCES"
                    ) ?: arrayListOf()



                val userInfo = UserPreference(
                    nickname = userNickname,
                    language = userLanguage,
                    preferences = userPreferences,
                    allergies = selectedAllergies
                )


                val uid =
                    auth.currentUser?.uid
                        ?: userNickname



                db.collection("users")
                    .document(uid)
                    .set(userInfo)
                    .addOnSuccessListener {


                        val nextIntent =
                            Intent(
                                this,
                                WelcomeActivity::class.java
                            ).apply {

                                putExtra(
                                    "USER_NICKNAME",
                                    userNickname
                                )
                            }


                        startActivity(nextIntent)

                        finish()

                    }
            }
        }
    }



    private fun loadUserAllergies(
        cards: List<LinearLayout>,
        btnNone: TextView
    ) {

        val uid =
            auth.currentUser?.uid ?: return


        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->


                val allergies =
                    document.get("allergies")
                            as? List<String>
                        ?: emptyList()

                val detailAllergies = listOf(
                    "CORN",
                    "CHICKEN",
                    "BEEF",
                    "PORK",
                    "TOMATO",
                    "MUSHROOM",
                    "COCONUT",
                    "KIWI",
                    "PEACH",
                    "BANANA",
                    "GARLIC",
                    "ONION",
                    "SPICES"
                )

                if (allergies.any { detailAllergies.contains(it) }) {
                    openDetailAllergy()
                }


                for (card in cards) {

                    when (card.id) {

                        R.id.cardPeanuts ->
                            card.isSelected =
                                allergies.contains("PEANUTS")

                        R.id.cardTreeNuts ->
                            card.isSelected =
                                allergies.contains("TREE_NUTS")

                        R.id.cardMilk ->
                            card.isSelected =
                                allergies.contains("MILK")

                        R.id.cardEggs ->
                            card.isSelected =
                                allergies.contains("EGGS")

                        R.id.cardSeafood ->
                            card.isSelected =
                                allergies.contains("SEAFOOD")

                        R.id.cardWheat ->
                            card.isSelected =
                                allergies.contains("WHEAT")

                        R.id.cardSoy ->
                            card.isSelected =
                                allergies.contains("SOY")

                        R.id.cardSesame ->
                            card.isSelected =
                                allergies.contains("SESAME")

                        R.id.cardCorn ->
                            card.isSelected =
                                allergies.contains("CORN")

                        R.id.cardChicken ->
                            card.isSelected =
                                allergies.contains("CHICKEN")

                        R.id.cardBeef ->
                            card.isSelected =
                                allergies.contains("BEEF")

                        R.id.cardPork ->
                            card.isSelected =
                                allergies.contains("PORK")

                        R.id.cardTomato ->
                            card.isSelected =
                                allergies.contains("TOMATO")

                        R.id.cardMushroom ->
                            card.isSelected =
                                allergies.contains("MUSHROOM")

                        R.id.cardCoconut ->
                            card.isSelected =
                                allergies.contains("COCONUT")

                        R.id.cardKiwi ->
                            card.isSelected =
                                allergies.contains("KIWI")

                        R.id.cardPeach ->
                            card.isSelected =
                                allergies.contains("PEACH")

                        R.id.cardBanana ->
                            card.isSelected =
                                allergies.contains("BANANA")

                        R.id.cardGarlic ->
                            card.isSelected =
                                allergies.contains("GARLIC")

                        R.id.cardOnion ->
                            card.isSelected =
                                allergies.contains("ONION")

                        R.id.cardSpices ->
                            card.isSelected =
                                allergies.contains("SPICES")
                    }
                }


                btnNone.isSelected =
                    allergies.contains("NONE")


                findViewById<AppCompatButton>(R.id.btnComplete)
                    .isEnabled =
                    allergies.isNotEmpty()
            }
    }



    private fun updateAllergies(
        allergies: MutableList<String>
    ) {

        val uid =
            auth.currentUser?.uid ?: return


        db.collection("users")
            .document(uid)
            .update(
                "allergies",
                allergies
            )
            .addOnSuccessListener {

                finish()

            }
    }
    private fun openDetailAllergy() {

        val gridDefaultAllergy =
            findViewById<GridLayout>(R.id.gridDefaultAllergy)

        val cardOther =
            findViewById<LinearLayout>(R.id.cardOther)


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


        gridDefaultAllergy.removeView(cardOther)


        for (detailCard in detailedCards) {

            gridDefaultAllergy.removeView(detailCard)

            detailCard.visibility = View.VISIBLE

            gridDefaultAllergy.addView(detailCard)
        }
    }
}