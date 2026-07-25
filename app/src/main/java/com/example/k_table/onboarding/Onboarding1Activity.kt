package com.example.k_table.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.k_table.R

class Onboarding1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding1)

        // 3초 후 두 번째 온보딩 화면으로 이동
        Handler(Looper.getMainLooper()).postDelayed({

            val intent = Intent(this, Onboarding2Activity::class.java)
            startActivity(intent)
            finish()

        }, 3000)
    }
}