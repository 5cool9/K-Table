package com.example.k_table

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val tvNickname = findViewById<TextView>(R.id.tvNickname)
        val btnGetStarted = findViewById<AppCompatButton>(R.id.btnGetStarted)

        // 이전에 입력했던 닉네임을 Intent로 받아옴 (없으면 기본값 "User" 출력)
        val nickname = intent.getStringExtra("USER_NICKNAME") ?: "User"
        tvNickname.text = nickname

        // Get Started 버튼 누르면 메인 화면으로 이동
        btnGetStarted.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // 온보딩 스택을 전부 비워서 뒤로 가기를 눌러도 회원가입 화면으로 돌아오지 않게 처리
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}