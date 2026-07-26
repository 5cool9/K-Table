package com.example.k_table

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.doAfterTextChanged

class Preference2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_2)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnNext = findViewById<AppCompatButton>(R.id.btnNext)
        val etNickname = findViewById<EditText>(R.id.etNickname)

        // 드롭다운에 들어갈 언어 데이터 설정
        val languages = arrayOf("Korean", "English")
        val spinner = findViewById<Spinner>(R.id.spinnerLanguage)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        spinner.adapter = adapter

        // 처음 화면에 들어왔을 때는 닉네임이 비어있으므로 버튼 비활성화
        btnNext.isEnabled = false

        etNickname.doAfterTextChanged { text ->
            val nickname = text.toString().trim()
            // 글자가 있으면 활성화, 없으면 비활성화
            btnNext.isEnabled = nickname.isNotEmpty()
        }

        // 상단 뒤로 가기 버튼 클릭 시
        btnBack.setOnClickListener {
            finish()
        }

        btnNext.setOnClickListener {
            val userNickname = etNickname.text.toString().trim()
            val userLanguage = spinner.selectedItem.toString()

            val intent = Intent(this, Preference3Activity::class.java).apply {
                putExtra("USER_NICKNAME", userNickname)
                putExtra("USER_LANGUAGE", userLanguage)
            }
            startActivity(intent)
        }
    }
}