package com.example.k_table

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class Preference2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_2)

        // 드롭다운(Spinner)에 들어갈 언어 데이터 설정
        val languages = arrayOf("Korean", "English")
        val spinner = findViewById<Spinner>(R.id.spinnerLanguage)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        spinner.adapter = adapter

        // 상단 뒤로 가기 버튼 클릭 시
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // 하단 Next 버튼 클릭 시 (다음 3단계 개인 설정 페이지로 이동)
        val btnNext = findViewById<AppCompatButton>(R.id.btnNext)
        btnNext.setOnClickListener {
            // TODO: 사용자가 입력한 닉네임과 언어 값을 저장하는 로직을 여기에 추가할 수 있습니다.

            // 다음 3단계 화면으로 이동 (예시: Preference3Activity)
            val intent = Intent(this, Preference3Activity::class.java)
            startActivity(intent)
        }
    }
}