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
import com.example.k_table.model.GeminiRequest
import com.example.k_table.model.Content
import com.example.k_table.model.Part
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import com.example.k_table.api.GeminiRetrofitClient
import com.example.k_table.BuildConfig
import com.example.k_table.home.UserDietProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScanResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanResultBinding
    private lateinit var adapter: ScanResultAdapter
    private lateinit var tts: TextToSpeech
    private var fullList: List<MenuScanResult> = emptyList()
    private var scanMenus: ArrayList<MenuScanResult> = arrayListOf()
    private var cachedUserProfile: UserDietProfile? = null

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

        val result = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(
                "scanResult",
                MenuScanResult::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra<MenuScanResult>("scanResult")
        }

        scanMenus = result ?: arrayListOf()

        fullList = scanMenus

        setupTabs()
        applyFilter(TabFilter.ALL)

        fetchUserProfileAndAnalyze(scanMenus)
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


    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

    private fun fetchUserProfileAndAnalyze(
        menus: List<MenuScanResult>
    ) {

        val db = FirebaseFirestore.getInstance()

        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                val allergies =
                    document.get("allergies") as? List<String>
                        ?: emptyList()

                val preferences =
                    document.get("preferences") as? List<String>
                        ?: emptyList()


                analyzeMenuWithGemini(
                    menus,
                    allergies,
                    preferences
                )
            }
    }

    private fun analyzeMenuWithGemini(
        menus: List<MenuScanResult>,
        allergies: List<String>,
        preferences: List<String>
    )
    {

        CoroutineScope(Dispatchers.IO).launch {

            val menuNames = menus.joinToString(", ") {
                it.koreanName
            }
            val allergyText = allergies.joinToString(", ")
            val preferenceText = preferences.joinToString(", ")

            val prompt = """
너는 식단 제한 사용자를 위한 음식 분석 AI이다.

사용자 정보

- 알레르기 코드:
$allergyText

- 식단 선호/제한 코드:
$preferenceText


아래는 음식점 메뉴판에서 추출한 메뉴 목록이다.

$menuNames


각 메뉴를 사용자의 알레르기와 식단 제한 조건 기준으로 분석해라.


판단 규칙

1. 사용자의 조건과 메뉴 특성을 고려하여 판단한다.

2. SUITABLE
- 사용자가 안전하게 먹을 가능성이 높은 메뉴
- 일반적인 재료 기준으로 알레르기 유발 가능성이 낮은 메뉴

3. CAUTION
- 메뉴 이름만으로 정확한 재료 판단이 어려운 경우
- 양념, 소스, 육수, 조리 방식에 따라 달라질 수 있는 경우
- 식당에 확인이 필요한 경우

4. UNSUITABLE
- 사용자의 알레르기 유발 가능성이 높은 재료가 일반적으로 포함되는 경우
- 사용자의 식단 제한(VEGAN, VEGETARIAN 등)을 명확하게 위반하는 경우


추가 규칙

- 메뉴 이름은 입력받은 이름을 그대로 유지한다.
- 없는 메뉴를 추가하지 않는다.
- 모든 메뉴를 반드시 분석한다.
- 확실하지 않은 경우 제외하지 말고 CAUTION으로 분류한다.
- CAUTION인 경우 questionKo에 사장님께 확인할 질문을 작성한다.
- UNSUITABLE인 경우 questionKo는 null이다.
- SUITABLE인 경우 questionKo는 null이다.


반드시 JSON 배열만 출력한다.

중요:
- 절대 ```json 같은 코드블록을 사용하지 않는다.
- 설명 문장을 추가하지 않는다.
- JSON 배열 외 다른 텍스트를 출력하지 않는다.


출력 형식:

[
  {
    "id":"",
    "koreanName":"",
    "englishName":"",
    "status":"SUITABLE",
    "questionKo":null
  }
]

status는 반드시 아래 세 값 중 하나만 사용한다.

SUITABLE
CAUTION
UNSUITABLE

""".trimIndent()


            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(
                                text = prompt
                            )
                        )
                    )
                )
            )


            val response =
                GeminiRetrofitClient.api.generate(
                    apiKey = BuildConfig.GEMINI_SCAN_API_KEY,
                    request = request
                )


            if (response.isSuccessful) {

                val resultText =
                    response.body()
                        ?.candidates
                        ?.firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull()
                        ?.text


                resultText?.let {

                    val geminiResult =
                        parseGeminiResult(it)

                    if (geminiResult.isNotEmpty()) {

                        runOnUiThread {
                            fullList = geminiResult
                            updateTabCounts()
                            applyFilter(TabFilter.ALL)
                        }

                    }
                }

            } else {

                Log.e(
                    "GEMINI_ANALYZE",
                    "error=${response.code()}"
                )

            }
        }
    }
    private fun parseGeminiResult(
        json: String
    ): List<MenuScanResult> {

        val cleanJson = json
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val type = object : TypeToken<List<MenuScanResult>>() {}.type

        return try {
            Gson().fromJson(
                cleanJson,
                type
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
}