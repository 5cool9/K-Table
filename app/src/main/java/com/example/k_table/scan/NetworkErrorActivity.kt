package com.example.k_table.scan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.k_table.MainActivity
import com.example.k_table.databinding.ActivityNetworkErrorBinding

class NetworkErrorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNetworkErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNetworkErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 다시 촬영페이지로 이동
        binding.btnRetry.setOnClickListener {

            val intent = Intent(
                this,
                MainActivity::class.java
            )

            intent.putExtra(
                "openScan",
                true
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            finish()
        }

        // 홈으로
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }
}