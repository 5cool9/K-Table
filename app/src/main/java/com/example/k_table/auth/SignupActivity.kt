package com.example.k_table.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.k_table.Preference2Activity
import com.example.k_table.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // 뒤로가기
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 다음 버튼
        binding.btnNext.setOnClickListener {

            val email =
                binding.etEmail.text.toString().trim()
            val password =
                binding.etPassword.text.toString().trim()


            if(email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "이메일과 비밀번호를 입력해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            signup(
                email,
                password
            )
        }
    }

    private fun signup(
        email: String,
        password: String
    ) {

        firebaseAuth
            .createUserWithEmailAndPassword(
                email,
                password
            )
            .addOnCompleteListener { task ->

                if(task.isSuccessful) {

                    val user = firebaseAuth.currentUser

                    val intent =
                        Intent(
                            this,
                            Preference2Activity::class.java
                        )

                    startActivity(intent)
                    finish()

                } else {
                    val message = when {

                        task.exception?.message?.contains("email address is already in use", true) == true ->
                            "이미 가입된 이메일입니다."

                        task.exception?.message?.contains("badly formatted", true) == true ->
                            "이메일 형식이 올바르지 않습니다."

                        task.exception?.message?.contains("password", true) == true ->
                            "비밀번호는 6자 이상 입력해주세요."

                        else ->
                            "회원가입에 실패했습니다. 다시 시도해주세요."
                    }


                    Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}