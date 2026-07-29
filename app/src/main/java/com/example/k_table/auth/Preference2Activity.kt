package com.example.k_table

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.doAfterTextChanged
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Preference2Activity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_2)

        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)

        if (isEditMode) {

            tvTitle.text = "언어 설정"
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnNext = findViewById<AppCompatButton>(R.id.btnNext)
        val etNickname = findViewById<EditText>(R.id.etNickname)


        if (isEditMode) {

            btnNext.text = "완료"

            loadUserInfo(
                etNickname
            )

        } else {

            btnNext.isEnabled = false
        }


        etNickname.doAfterTextChanged {

            btnNext.isEnabled =
                etNickname.text.toString().trim().isNotEmpty()

        }


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


        btnNext.setOnClickListener {

            val userNickname =
                etNickname.text.toString().trim()

            val userLanguage = "Korean"

            if (isEditMode) {

                updateUserInfo(
                    userNickname,
                    userLanguage
                )

            } else {

                val intent =
                    Intent(
                        this,
                        Preference3Activity::class.java
                    ).apply {

                        putExtra(
                            "USER_NICKNAME",
                            userNickname
                        )

                        putExtra(
                            "USER_LANGUAGE",
                            userLanguage
                        )
                    }

                startActivity(intent)
            }
        }
    }


    private fun loadUserInfo(
        etNickname: EditText
    ) {

        val uid =
            auth.currentUser?.uid ?: return


        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val nickname =
                        document.getString("nickname") ?: ""


                    etNickname.setText(nickname)


                    findViewById<AppCompatButton>(R.id.btnNext)
                        .isEnabled = true
                }
            }
    }


    private fun updateUserInfo(
        nickname: String,
        language: String
    ) {

        val uid =
            auth.currentUser?.uid ?: return


        val updateData = hashMapOf(
            "nickname" to nickname,
            "language" to language
        )


        db.collection("users")
            .document(uid)
            .update(updateData as Map<String, Any>)
            .addOnSuccessListener {

                finish()

            }
    }
}