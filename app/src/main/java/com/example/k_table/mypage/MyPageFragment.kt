package com.example.k_table

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var btnEdit: MaterialButton
    private lateinit var btnLogout: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // XML 파일명이 fragment_mypage.xml이라면 맞음
        setContentView(R.layout.fragment_mypage)

        btnEdit = findViewById(R.id.btnEdit)
        btnLogout = findViewById(R.id.btnLogout)

        // 프로필 편집
        btnEdit.setOnClickListener {
            Toast.makeText(this, "프로필 편집 화면", Toast.LENGTH_SHORT).show()

            // 나중에 EditProfileActivity 만들면
            // startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // 로그아웃
        btnLogout.setOnClickListener {
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}