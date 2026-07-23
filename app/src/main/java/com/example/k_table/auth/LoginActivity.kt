package com.example.k_table

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 이메일 로그인 버튼 클릭 시
        val btnEmailLogin = findViewById<Button>(R.id.btnEmailLogin)
        btnEmailLogin.setOnClickListener {
            navigateToMain()
        }

        // 구글 로그인 버튼 클릭 시
        val btnGoogleLogin = findViewById<LinearLayout>(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            navigateToMain()
        }

        // 회원가입 버튼 클릭 시
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        tvSignUp.setOnClickListener {
            // TODO: 회원가입 화면으로 이동하는 코드 작성 예정
        }
    }

    // 메인 화면(탭바가 있는 곳)으로 이동하고 로그인 화면은 종료하는 함수
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}