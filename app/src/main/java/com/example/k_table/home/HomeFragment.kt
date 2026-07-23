package com.example.k_table.home

import com.example.k_table.BuildConfig
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.k_table.R
import com.example.k_table.RestaurantAdapter
import com.example.k_table.api.RetrofitClient
import com.example.k_table.model.KakaoPlace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import com.example.k_table.model.GeminiRequest
import com.example.k_table.api.GeminiRetrofitClient
import com.example.k_table.model.Content
import com.example.k_table.model.Part
import com.example.k_table.model.GeminiResponse
import com.example.k_table.model.Restaurant
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class HomeFragment : Fragment(R.layout.fragment_home) {

    // 화면에 추천 식당 목록을 표시하기 위한 RecyclerView 관련 변수
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantAdapter
    // 현재 추천된 식당 데이터를 저장하는 리스트
    private val restaurantList = mutableListOf<Restaurant>()
    // 사용자 현재 위치 정보를 가져오기 위한 클라이언트
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        // 사용자 위치 조회 객체 초기화
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(
                requireContext()
            )

        // RecyclerView 초기 설정
        recyclerView =
            view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        adapter = RestaurantAdapter(restaurantList)

        recyclerView.adapter = adapter

        val btnSearch: Button =
            view.findViewById(R.id.btnSearch)

        btnSearch.setOnClickListener {

            Toast.makeText(
                requireContext(),
                "식당 검색 시작",
                Toast.LENGTH_SHORT
            ).show()

            fetchRestaurantsFromKakao()

        }

    }

    // Gemini API 서버 오류 발생 시 일정 시간 후 재시도 처리
    private suspend fun callGeminiWithRetry(
        apiCall: suspend () -> retrofit2.Response<GeminiResponse>
    ): retrofit2.Response<GeminiResponse> {

        repeat(3) { attempt ->

            val response = apiCall()

            if (response.isSuccessful) {
                return response
            }

            if (response.code() == 503) {

                Log.d(
                    "GEMINI_RETRY",
                    "${attempt + 1}번째 재시도"
                )

                kotlinx.coroutines.delay(3000)
            } else {
                return response
            }
        }

        throw Exception("Gemini 서버 응답 실패")
    }

    // 현재 사용자 위치 기반 카카오 API에서 주변 식당 검색
    // 현재 위치 기반 카카오 API에서 주변 식당 검색
    private fun fetchRestaurantsFromKakao() {


        // 기본 위치 (서울 시청)
        var latitude = "37.5665"
        var longitude = "126.9780"

        // 위치 권한 확인
        val hasPermission =
            requireContext().checkSelfPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED


        if (hasPermission) {

            // 현재 위치 가져오기
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->


                    if (location != null
                        && location.latitude in 33.0..39.0
                        && location.longitude in 124.0..132.0
                    ) {


                        latitude =
                            location.latitude.toString()

                        longitude =
                            location.longitude.toString()


                        Log.d(
                            "LOCATION",
                            "사용자 위치 사용 : $latitude / $longitude"
                        )

                    } else {

                        Log.d(
                            "LOCATION",
                            "비정상 위치 또는 위치 없음 → 서울 기본 위치 사용"
                        )
                    }

                    requestRestaurantSearch(
                        latitude,
                        longitude
                    )

                }

        } else {

            Log.d(
                "LOCATION",
                "권한 없음 → 서울 기본 위치 사용"
            )

            requestRestaurantSearch(
                latitude,
                longitude
            )

        }

    }

    // 카카오 API 식당 검색 요청
    private fun requestRestaurantSearch(
        latitude: String,
        longitude: String
    ) {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val response =
                    RetrofitClient.api.searchRestaurant(

                        key =
                            "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}",

                        longitude =
                            longitude,

                        latitude =
                            latitude

                    )

                if (response.isSuccessful) {

                    val places =
                        response.body()?.documents
                            ?: emptyList()


                    Log.d(
                        "KAKAO_RESULT",
                        "검색된 식당 : ${places.size}"
                    )


                    analyzeRestaurantsWithGemini(
                        places.take(10)
                    )

                } else {

                    Log.e(
                        "KAKAO_ERROR",
                        "code : ${response.code()}"
                    )

                }

            } catch (e: Exception) {

                Log.e(
                    "KAKAO_ERROR",
                    e.toString()
                )

            }

        }

    }

    // 현재 위치 기반으로 검색된 식당 정보를 Gemini에게 전달 후 사용자 식단 조건에 맞는 식당 추천 결과 생성
    private fun analyzeRestaurantsWithGemini(
        places: List<KakaoPlace>
    ) {

        Log.d(
            "GEMINI_TEST", "Gemini 호출 시작 : ${places.size}"
        )

        CoroutineScope(Dispatchers.IO).launch {

            try {
                val geminiKey = BuildConfig.GEMINI_API_KEY
                val restaurantData = places.joinToString("\n") {

                    """
                        식당명 : ${it.place_name}
                        카테고리 : ${it.category_name}
                        주소 : ${it.road_address_name}
                        전화번호 : ${it.phone}
                        """.trimIndent()
                }

                // 사용자 식당 조건, 카카오 식당 정보를 기반 추천 가능 식당을 JSON 형태로 반환하도록 요청
                val prompt = """
너는 식단 제한 사용자를 위한 음식점 추천 AI이다.

사용자 정보
- 종교 : 이슬람
- 식단 : 할랄(Halal) 음식만 섭취 가능

할랄 기준
- 돼지고기 사용 금지
- 알코올 사용 금지
- 할랄 인증 육류 사용 식당 선호
- 중동 음식, 터키 음식, 인도 음식, 할랄 전문점은 우선 고려한다.

아래는 카카오 API에서 가져온 실제 식당 목록이다.

$restaurantData

규칙

1. 반드시 아래 식당 목록 안에서만 추천한다.
2. 존재하지 않는 식당은 절대 만들지 않는다.
3. 사용자가 방문하기 적합한 식당만 추천한다.
4. 추천 가능한 식당이 없다면 빈 배열 []을 반환한다.
5. 최대 5개까지만 추천한다.
6. 식당명과 주소는 입력값을 그대로 사용한다.
7. feature에는 추천 이유를 한 문장으로 작성한다.

판단은 일반적으로 알려진 음식 종류와 식당명을 바탕으로 수행한다.
확실하지 않은 경우에도
일반적으로 알려진 음식 종류와
식당명 및 카테고리를 바탕으로
가장 가능성이 높은 식당을 추천한다.

추천 가능한 식당을
최대 5개 반환한다.

반드시 JSON 배열만 출력한다.

[
  {
    "name":"",
    "address":"",
    "rating":4.5,
    "feature":""
  }
]

""".trimIndent()


                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(prompt)
                            )
                        )
                    )
                )

                val response =
                    callGeminiWithRetry {

                        GeminiRetrofitClient.api.generate(
                            apiKey = geminiKey,
                            request = request
                        )

                    }

                Log.d(
                    "GEMINI_HTTP", "code=${response.code()}"
                )

                Log.d(
                    "GEMINI_HTTP", response.errorBody()?.string() ?: "no error"
                )


                if (response.isSuccessful) {

                    val result =
                        response.body()?.candidates
                            ?.firstOrNull()
                            ?.content
                            ?.parts
                            ?.firstOrNull()
                            ?.text
                            ?: "[]"

                    Log.d(
                        "GEMINI_RESULT",
                        result
                    )

                    updateRecyclerView(result)
                }

                withContext(Dispatchers.Main) {

                    Toast.makeText(
                        requireContext(), "Gemini 응답 성공", Toast.LENGTH_LONG
                    ).show()

                }

            } catch (e: Exception) {

                Log.e(
                    "GEMINI_ERROR", e.toString()
                )

                e.printStackTrace()

                withContext(Dispatchers.Main) {

                    Toast.makeText(
                        requireContext(), "Gemini 오류 발생", Toast.LENGTH_LONG
                    ).show()

                }

            }

        }

    }

    // Gemini 응답 JSON 데이터를 Restaurant 객체로 변환 후 RecyclerView 갱신
    private suspend fun updateRecyclerView(
        jsonString: String
    ) {

        try {

            val cleanJson =
                jsonString
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

            val jsonArray =
                JSONArray(cleanJson)

            val newList = mutableListOf<Restaurant>()

            for (i in 0 until jsonArray.length()) {

                val obj = jsonArray.getJSONObject(i)

                newList.add(

                    Restaurant(

                        name = obj.getString("name"),

                        address = obj.getString("address"),

                        rating = obj.getDouble("rating"),

                        feature = obj.getString("feature")

                    )

                )

            }

            withContext(Dispatchers.Main) {

                restaurantList.clear()

                restaurantList.addAll(newList)

                adapter.notifyDataSetChanged()
            }

        } catch (e: Exception) {

            Log.e(
                "JSON_ERROR", e.message ?: ""

            )

        }

    }

}