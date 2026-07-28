package com.example.k_table

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.k_table.home.HomeFragment
import com.example.k_table.scan.ScanFragment
import com.example.k_table.MyPageFragment

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContentView(R.layout.activity_main)


        // 앱 실행 시 기본 화면 = 홈, 스캔화면에서 다시 시도 눌렀을 때는 사진 선택 화면으로 이동
        val openScan =
            intent.getBooleanExtra(
                "openScan",
                false
            )
        if(openScan){
            changeFragment(
                ScanFragment()
            )
        }else{

            changeFragment(
                HomeFragment()
            )
        }


        val homeTab =
            findViewById<ImageButton>(R.id.homeTab)

        val scanTab =
            findViewById<ImageButton>(R.id.scanTab)

        val myPageTab =
            findViewById<ImageButton>(R.id.myPageTab)



        homeTab.setOnClickListener {

            changeFragment(HomeFragment())

        }



        scanTab.setOnClickListener {

            changeFragment(ScanFragment())

        }



        myPageTab.setOnClickListener {

            changeFragment(MyPageFragment())

        }


    }

    private fun changeFragment(fragment: Fragment){

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                fragment
            )
            .commit()

    }

}