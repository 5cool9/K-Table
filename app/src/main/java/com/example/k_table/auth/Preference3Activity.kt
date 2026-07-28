package com.example.k_table

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Preference3Activity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_3)

        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)

        if (isEditMode) {

            tvTitle.text = "선호 설정"
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnNone = findViewById<TextView>(R.id.btnNone)
        val btnNext = findViewById<AppCompatButton>(R.id.btnNext)

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

        val normalCards = listOf(
            findViewById<TextView>(R.id.btnHalal),
            findViewById<TextView>(R.id.btnVegetarian),
            findViewById<TextView>(R.id.btnVegan),
            findViewById<TextView>(R.id.btnMsgFree),
            findViewById<TextView>(R.id.btnGlutenFree),
            findViewById<TextView>(R.id.btnPorkFree),
            findViewById<TextView>(R.id.btnAlcoholFree)
        )


        fun updateState() {

            val isAnyNormalSelected =
                normalCards.any { it.isSelected }

            val isNoneSelected =
                btnNone.isSelected


            btnNext.isEnabled =
                isAnyNormalSelected || isNoneSelected


            for (card in normalCards) {

                card.setTextColor(
                    if (card.isSelected)
                        Color.WHITE
                    else
                        Color.parseColor("#333333")
                )
            }


            btnNone.setTextColor(
                if (btnNone.isSelected)
                    Color.WHITE
                else
                    Color.parseColor("#008000")
            )
        }


        if (isEditMode) {

            btnNext.text = "완료"

            loadUserPreferences(
                normalCards,
                btnNone
            )

        } else {

            updateState()
        }


        for (card in normalCards) {

            card.setOnClickListener {

                btnNone.isSelected = false

                card.isSelected =
                    !card.isSelected

                updateState()
            }
        }


        btnNone.setOnClickListener {

            for (card in normalCards) {
                card.isSelected = false
            }

            btnNone.isSelected =
                !btnNone.isSelected

            updateState()
        }



        btnNext.setOnClickListener {


            val selectedPreferences =
                ArrayList<String>()


            val preferenceKeyMap = mapOf(
                R.id.btnHalal to "HALAL",
                R.id.btnVegetarian to "VEGETARIAN",
                R.id.btnVegan to "VEGAN",
                R.id.btnMsgFree to "MSG_FREE",
                R.id.btnGlutenFree to "GLUTEN_FREE",
                R.id.btnPorkFree to "PORK_FREE",
                R.id.btnAlcoholFree to "ALCOHOL_FREE"
            )


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



            if (isEditMode) {


                updatePreferences(
                    selectedPreferences
                )


            } else {


                val userNickname =
                    intent.getStringExtra("USER_NICKNAME")
                        ?: "USER"


                val userLanguage =
                    intent.getStringExtra("USER_LANGUAGE")
                        ?: "Korean"



                val intent =
                    Intent(
                        this,
                        Preference4Activity::class.java
                    ).apply {

                        putExtra(
                            "USER_NICKNAME",
                            userNickname
                        )

                        putExtra(
                            "USER_LANGUAGE",
                            userLanguage
                        )

                        putStringArrayListExtra(
                            "USER_PREFERENCES",
                            selectedPreferences
                        )
                    }


                startActivity(intent)
            }
        }
    }



    private fun loadUserPreferences(
        normalCards: List<TextView>,
        btnNone: TextView
    ) {

        val uid =
            auth.currentUser?.uid ?: return


        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->


                val savedPreferences =
                    document.get("preferences")
                            as? List<String>
                        ?: emptyList()



                for (card in normalCards) {

                    when (card.id) {

                        R.id.btnHalal ->
                            card.isSelected =
                                savedPreferences.contains("HALAL")


                        R.id.btnVegetarian ->
                            card.isSelected =
                                savedPreferences.contains("VEGETARIAN")


                        R.id.btnVegan ->
                            card.isSelected =
                                savedPreferences.contains("VEGAN")


                        R.id.btnMsgFree ->
                            card.isSelected =
                                savedPreferences.contains("MSG_FREE")


                        R.id.btnGlutenFree ->
                            card.isSelected =
                                savedPreferences.contains("GLUTEN_FREE")


                        R.id.btnPorkFree ->
                            card.isSelected =
                                savedPreferences.contains("PORK_FREE")


                        R.id.btnAlcoholFree ->
                            card.isSelected =
                                savedPreferences.contains("ALCOHOL_FREE")
                    }
                }


                btnNone.isSelected =
                    savedPreferences.contains("None")



                for (card in normalCards) {

                    card.setTextColor(
                        if (card.isSelected)
                            Color.WHITE
                        else
                            Color.parseColor("#333333")
                    )
                }


                btnNone.setTextColor(
                    if (btnNone.isSelected)
                        Color.WHITE
                    else
                        Color.parseColor("#008000")
                )


                findViewById<AppCompatButton>(R.id.btnNext)
                    .isEnabled =
                    savedPreferences.isNotEmpty()
            }
    }



    private fun updatePreferences(
        preferences: ArrayList<String>
    ) {

        val uid =
            auth.currentUser?.uid ?: return


        db.collection("users")
            .document(uid)
            .update(
                "preferences",
                preferences
            )
            .addOnSuccessListener {

                finish()

            }
    }
}