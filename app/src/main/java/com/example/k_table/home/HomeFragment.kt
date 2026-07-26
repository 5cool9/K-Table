package com.example.k_table.home

import com.example.k_table.BuildConfig
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Firestore에서 불러온 사용자 식단 프로필
data class UserDietProfile(
    val allergies: List<String>,
    val preferences: List<String>
)

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantAdapter
    private val restaurantList = mutableListOf<Restaurant>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // 한번 불러온 프로필은 재사용 (버튼 다시 눌러도 Firestore 재조회 안 하도록)
    private var cachedUserProfile: UserDietProfile? = null

    // 위치 권한 요청 런처 - 반드시 Fragment 필드로 등록 (onViewCreated 안 X)
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchCurrentLocationAndSearch()
            } else {
                Toast.makeText(
                    requireContext(),
                    "위치 권한이 거부되어 기본 위치로 검색합니다.",
                    Toast.LENGTH_SHORT
                ).show()
                startSearch("37.5665", "126.9780")
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        recyclerView = view.findViewById(R.id.rvRestaurantList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RestaurantAdapter(restaurantList)
        recyclerView.adapter = adapter

        val btnLocation = view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnLocation)
        btnLocation.setOnClickListener {
            requestLocationPermissionAndSearch()
        }

        // 홈 화면 진입 시 서울 기본 좌표로 자동 추천 시작
        startSearch("37.5665", "126.9780")
    }

    // 위치 버튼 클릭 시 권한 체크 후 분기
    private fun requestLocationPermissionAndSearch() {
        val hasPermission = requireContext().checkSelfPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            fetchCurrentLocationAndSearch()
        } else {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 실제 현재 위치를 가져와서 검색
    private fun fetchCurrentLocationAndSearch() {

        val hasPermission = requireContext().checkSelfPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.d("LOCATION", "권한 없음 → 기본 위치 사용")
            startSearch("37.5665", "126.9780")
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null
                && location.latitude in 33.0..39.0
                && location.longitude in 124.0..132.0
            ) {
                Log.d("LOCATION", "사용자 위치 사용 : ${location.latitude} / ${location.longitude}")
                startSearch(location.latitude.toString(), location.longitude.toString())
            } else {
                Log.d("LOCATION", "비정상 위치 → 서울 기본 위치 사용")
                Toast.makeText(requireContext(), "현재 위치를 확인할 수 없어 기본 위치로 검색합니다.", Toast.LENGTH_SHORT).show()
                startSearch("37.5665", "126.9780")
            }
        }
    }

    // 프로필이 캐시되어 있으면 바로, 없으면 Firestore에서 불러온 뒤 검색 시작
    private fun startSearch(latitude: String, longitude: String) {
        val profile = cachedUserProfile
        if (profile != null) {
            requestRestaurantSearch(latitude, longitude, profile)
        } else {
            fetchUserProfileFromFirestore { fetchedProfile ->
                cachedUserProfile = fetchedProfile
                requestRestaurantSearch(latitude, longitude, fetchedProfile)
            }
        }
    }

    // Firestore에서 사용자 알레르기/식단 정보 불러오기
    private fun fetchUserProfileFromFirestore(onResult: (UserDietProfile) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Log.d("FIRESTORE", "로그인 정보 없음 → 기본 프로필 사용")
            onResult(UserDietProfile(emptyList(), emptyList()))
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                @Suppress("UNCHECKED_CAST")
                val allergies = doc.get("allergies") as? List<String> ?: emptyList()

                @Suppress("UNCHECKED_CAST")
                val preferences = doc.get("preferences") as? List<String> ?: emptyList()

                Log.d("FIRESTORE", "프로필 로드 완료 : allergies=$allergies, preferences=$preferences")

                onResult(UserDietProfile(allergies, preferences))
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE_ERROR", e.toString())
                onResult(UserDietProfile(emptyList(), emptyList()))
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
                Log.d("GEMINI_RETRY", "${attempt + 1}번째 재시도")
                kotlinx.coroutines.delay(3000)
            } else {
                return response
            }
        }

        throw Exception("Gemini 서버 응답 실패")
    }

    // 카카오 API로 좌표 기반 주변 식당 검색
    private fun requestRestaurantSearch(
        latitude: String,
        longitude: String,
        profile: UserDietProfile
    ) {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val allPlaces = mutableListOf<KakaoPlace>()

                for (page in 1..3) {

                    val response = RetrofitClient.api.searchRestaurant(
                        key = "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}",
                        longitude = longitude,
                        latitude = latitude,
                        page = page
                    )

                    if (response.isSuccessful) {

                        val places =
                            response.body()?.documents ?: emptyList()

                        Log.d(
                            "KAKAO_PAGE",
                            "page=$page / ${places.size}개"
                        )

                        allPlaces.addAll(places)

                    } else {

                        Log.e(
                            "KAKAO_ERROR",
                            "page=$page code=${response.code()}"
                        )
                    }
                }
                Log.d(
                    "KAKAO_RESULT",
                    "총 검색된 식당 : ${allPlaces.size}"
                )

                val uniquePlaces =
                    allPlaces.distinctBy { it.place_name }

                Log.d(
                    "KAKAO_RESULT",
                    "중복 제거 후 : ${uniquePlaces.size}"
                )

                analyzeRestaurantsWithGemini(
                    uniquePlaces,
                    profile
                )

            } catch (e: Exception) {

                Log.e(
                    "KAKAO_ERROR",
                    e.toString()
                )
            }
        }
    }

    // 검색된 식당 목록 + 사용자 프로필을 Gemini에 전달해 추천 결과 생성
    private fun analyzeRestaurantsWithGemini(
        places: List<KakaoPlace>,
        profile: UserDietProfile
    ) {
        Log.d("GEMINI_TEST", "Gemini 호출 시작 : ${places.size}")

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

                val prompt = buildPrompt(profile, restaurantData)

                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(prompt))))
                )

                val response = callGeminiWithRetry {
                    GeminiRetrofitClient.api.generate(apiKey = geminiKey, request = request)
                }

                Log.d("GEMINI_HTTP", "code=${response.code()}")

                if (response.isSuccessful) {
                    val result = response.body()?.candidates
                        ?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "[]"

                    Log.d("GEMINI_RESULT", result)
                    updateRecyclerView(result)
                }

            } catch (e: Exception) {
                Log.e("GEMINI_ERROR", e.toString())
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "추천 생성 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Firestore에서 가져온 실제 사용자 프로필 기반으로 프롬프트 동적 생성
    private fun buildPrompt(profile: UserDietProfile, restaurantData: String): String {

        val allergyText =
            if (profile.allergies.isEmpty() || profile.allergies.contains("NONE"))
                "없음"
            else
                profile.allergies.joinToString(", ")

        val preferenceText =
            if (profile.preferences.isEmpty())
                "특별한 제한 없음"
            else
                profile.preferences.joinToString(", ")
        return """
너는 식단 제한 사용자를 위한 음식점 추천 AI이다.

사용자 정보 (영문 코드로 제공됨)
- 알레르기 코드 : $allergyText
  (예 : PEANUTS=땅콩, TREE_NUTS=견과류, MILK=우유, EGGS=계란, SEAFOOD=해산물, WHEAT=밀,
   SOY=대두, SESAME=참깨, CORN=옥수수, CHICKEN=닭고기, BEEF=소고기, PORK=돼지고기,
   TOMATO=토마토, MUSHROOM=버섯, COCONUT=코코넛, KIWI=키위, PEACH=복숭아,
   BANANA=바나나, GARLIC=마늘, ONION=양파, SPICES=향신료)

- 식단 선호/제한 코드 : $preferenceText
  (예 : VEGETARIAN=채식, VEGAN=비건, HALAL=할랄, KOSHER=코셔 등 사용자가 선택한 값)

아래는 카카오 API에서 가져온 실제 식당 목록이다.

$restaurantData

규칙
1. 반드시 아래 식당 목록 안에서만 추천한다.
2. 존재하지 않는 식당은 절대 만들지 않는다.
3. 사용자의 알레르기 코드와 식단 선호 조건을 최대한 고려한다.
4. 식당명과 카테고리만으로 판단 가능한 경우에는 추천한다. 정보가 부족하다고 해서 제외하지 않는다.
5. 빈 배열([])은 정말 모든 식당이 명백하게 조건을 위반하는 경우에만 반환한다. 대부분의 경우에는 가장 적합한 식당을 최대 5개 추천한다.
6. 최대 5개까지만 추천한다.
7. 식당명과 주소는 입력값을 그대로 사용한다.
8. feature에는 추천 이유를 한 문장으로 작성한다.
9. tags에는 이 식당의 특징을 나타내는 태그를 0~2개 배열로 작성한다.
   사용 가능한 태그 : "HALAL", "PORK_FREE", "VEGAN"
   세 태그는 식당명이나 음식 종류로 일반적으로 추론 가능한 경우에는 붙여도 된다.
   확실하지 않더라도 가능성이 높으면 태그를 붙여라. 
   단, 명백히 틀린 태그는 붙이지 않는다.

판단은 일반적으로 알려진 음식 종류와 식당명, 카테고리를 바탕으로 가장 가능성이 높은 식당을 추천한다.

반드시 JSON 배열만 출력한다.

[
  {
    "name":"",
    "address":"",
    "feature":"",
    "tags":["HALAL","PORK_FREE"]
  }
]
""".trimIndent()
    }

    // Gemini 응답 JSON을 Restaurant 객체로 변환 후 RecyclerView 갱신
    private suspend fun updateRecyclerView(jsonString: String) {
        try {
            val cleanJson = jsonString
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonArray = JSONArray(cleanJson)
            val newList = mutableListOf<Restaurant>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val tagsArray = obj.optJSONArray("tags")
                val tags = mutableListOf<String>()
                if (tagsArray != null) {
                    for (j in 0 until tagsArray.length()) {
                        tags.add(tagsArray.getString(j))
                    }
                }
                newList.add(
                    Restaurant(
                        name = obj.getString("name"),
                        address = obj.getString("address"),
                        feature = obj.getString("feature"),
                        tags = tags
                    )
                )
            }

            Log.d("RESTAURANT_SIZE", "newList 개수 = ${newList.size}")

            withContext(Dispatchers.Main) {
                restaurantList.clear()
                restaurantList.addAll(newList)
                Log.d(
                    "RESTAURANT_SIZE",
                    "restaurantList 개수 = ${restaurantList.size}"
                )
                adapter.notifyDataSetChanged()
            }

        } catch (e: Exception) {
            Log.e("JSON_ERROR", e.message ?: "")
        }
    }
}