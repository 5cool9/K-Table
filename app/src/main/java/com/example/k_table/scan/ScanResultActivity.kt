package com.example.k_table.scan

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.k_table.databinding.ActivityScanResultBinding
import com.example.k_table.scan.ScanResultAdapter
import com.example.k_table.model.MenuScanResult
import com.example.k_table.model.SuitabilityStatus
import com.google.android.material.tabs.TabLayout
import java.util.Locale

class ScanResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanResultBinding
    private lateinit var adapter: ScanResultAdapter
    private lateinit var tts: TextToSpeech

    private var fullList: List<MenuScanResult> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.KOREAN
            }
        }

        adapter = ScanResultAdapter(tts)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ScanResultActivity)
            adapter = this@ScanResultActivity.adapter
        }

        binding.btnBack.setOnClickListener { finish() }

        // ---- 목업 데이터 (나중에 제미나이 응답으로 교체) ----
        fullList = mockData()
        setupTabs()
        applyFilter(TabFilter.ALL)
    }

    private enum class TabFilter { ALL, SUITABLE, CAUTION, UNSUITABLE }

    private fun setupTabs() {
        updateTabCounts()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val filter = when (tab.position) {
                    1 -> TabFilter.SUITABLE
                    2 -> TabFilter.CAUTION
                    3 -> TabFilter.UNSUITABLE
                    else -> TabFilter.ALL
                }
                applyFilter(filter)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun updateTabCounts() {
        val all = fullList.size
        val suitable = fullList.count { it.status == SuitabilityStatus.SUITABLE }
        val caution = fullList.count { it.status == SuitabilityStatus.CAUTION }
        val unsuitable = fullList.count { it.status == SuitabilityStatus.UNSUITABLE }

        binding.tabLayout.getTabAt(0)?.text = "전체 $all"
        binding.tabLayout.getTabAt(1)?.text = "적합 $suitable"
        binding.tabLayout.getTabAt(2)?.text = "주의 $caution"
        binding.tabLayout.getTabAt(3)?.text = "부적합 $unsuitable"
    }

    private fun applyFilter(filter: TabFilter) {
        val filtered = when (filter) {
            TabFilter.ALL -> fullList
            TabFilter.SUITABLE -> fullList.filter { it.status == SuitabilityStatus.SUITABLE }
            TabFilter.CAUTION -> fullList.filter { it.status == SuitabilityStatus.CAUTION }
            TabFilter.UNSUITABLE -> fullList.filter { it.status == SuitabilityStatus.UNSUITABLE }
        }
        adapter.submitList(filtered)
    }

    /**
     * Gemini 분석 결과를 받아오는 지점에서 호출 코드 우선 주석 처리
     *
     * fun onGeminiResultReceived(result: List<MenuScanResult>) {
     *     fullList = result
     *     updateTabCounts()
     *     applyFilter(TabFilter.ALL)
     * }
     */

    private fun mockData(): List<MenuScanResult> = listOf(
        MenuScanResult(
            id = "1", koreanName = "비빔밥", englishName = "Bibimbap",
            status = SuitabilityStatus.SUITABLE
        ),
        MenuScanResult(
            id = "2", koreanName = "불고기 덮밥", englishName = "Bulgogi Rice Bowl",
            status = SuitabilityStatus.CAUTION,
            questionKo = "갈비 양념에 청주나 맛술이 들어가나요?\n알코올을 없이 조리해 주실 수 있나요?"
        ),
        MenuScanResult(
            id = "3", koreanName = "김치볶음밥", englishName = "Kimchi Fried Rice",
            status = SuitabilityStatus.CAUTION,
            questionKo = "혹시 새우젓이나 액젓이 들어가나요?"
        ),
        MenuScanResult(
            id = "4", koreanName = "된장찌개 정식", englishName = "Soybean Paste Stew Set Meal",
            status = SuitabilityStatus.CAUTION,
            questionKo = "육수에 멸치나 고기가 들어가나요?"
        ),
        MenuScanResult(
            id = "5", koreanName = "돌솥비빔밥", englishName = "Hot Stone Bibimbap",
            status = SuitabilityStatus.SUITABLE
        ),
        MenuScanResult(
            id = "6", koreanName = "떡볶이", englishName = "Tteokbokki",
            status = SuitabilityStatus.CAUTION,
            questionKo = "어묵에 밀가루가 들어가나요?"
        ),
        MenuScanResult(
            id = "7", koreanName = "고등어구이 정식", englishName = "Grilled Mackerel Set Meal",
            status = SuitabilityStatus.SUITABLE
        ),
        MenuScanResult(
            id = "8", koreanName = "잡채밥", englishName = "Japchae Rice Bowl",
            status = SuitabilityStatus.UNSUITABLE,
            questionKo = "잡채에 들어가는 당면 성분을 확인해 주실 수 있나요?"
        )
    )

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}