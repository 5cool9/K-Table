package com.example.k_table

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.k_table.api.RetrofitClient
import com.example.k_table.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.k_table.model.Restaurant
import com.example.k_table.model.KakaoPlace
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.k_table.api.GeminiRetrofitClient
import com.example.k_table.api.GooglePlacesRetrofitClient
import com.example.k_table.home.UserDietProfile
import com.example.k_table.model.GeminiRequest
import com.example.k_table.model.Content
import com.example.k_table.model.GeminiResponse
import com.example.k_table.model.Part
import org.json.JSONArray
import kotlinx.coroutines.withContext


class SearchRestaurantActivity : AppCompatActivity() {

    private var searchKeyword = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantAdapter
    private val restaurantList = mutableListOf<Restaurant>()
    private lateinit var etSearch: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var btnClear: ImageView
    private lateinit var layoutLoading: LinearLayout
    private var cachedUserProfile: UserDietProfile? = null
    private lateinit var layoutError: LinearLayout
    private var isSearching = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search_restaurant)

        etSearch = findViewById(R.id.etSearchResult)
        btnBack = findViewById(R.id.btnBack)
        btnClear = findViewById(R.id.ivClearSearch)
        layoutLoading = findViewById(R.id.layoutLoading)
        layoutError = findViewById(R.id.layoutError)

        recyclerView = findViewById(R.id.rvRestaurantList)

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        adapter = RestaurantAdapter(restaurantList)

        recyclerView.adapter = adapter

        // HomeFragment에서 넘어온 검색어 받기
        searchKeyword =
            intent.getStringExtra("SEARCH_KEYWORD")
                ?: ""

        etSearch.setText(searchKeyword)

        btnBack.setOnClickListener {
            finish()
        }

        btnClear.setOnClickListener {
            etSearch.text.clear()
        }


        Log.d(
            "SEARCH_KEYWORD",
            searchKeyword
        )

        if (!isSearching && searchKeyword.isNotEmpty()) {

            isSearching = true

            searchAddress(searchKeyword)
        }
    }

    private fun searchAddress(keyword: String) {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val response =
                    RetrofitClient.api.searchAddress(
                        key = "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}",
                        keyword = keyword
                    )


                if (response.isSuccessful) {

                    val address =
                        response.body()
                            ?.documents
                            ?.firstOrNull()


                    if (address != null) {

                        Log.d(
                            "ADDRESS_RESULT",
                            """
                        주소 : ${address.address_name}
                        위도(y) : ${address.y}
                        경도(x) : ${address.x}
                        """.trimIndent()
                        )
                        requestRestaurantSearch(
                            address.y,
                            address.x
                        )

                    } else {

                        Log.d(
                            "ADDRESS_RESULT",
                            "검색 결과 없음"
                        )
                    }

                } else {

                    Log.e(
                        "ADDRESS_ERROR",
                        "code=${response.code()}"
                    )

                }

            } catch (e: Exception) {

                Log.e(
                    "ADDRESS_ERROR",
                    e.toString()
                )
            }
        }
    }

    private fun requestRestaurantSearch(
        latitude: String,
        longitude: String
    ) {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val allPlaces = mutableListOf<KakaoPlace>()


                for (page in 1..3) {

                    val response =
                        RetrofitClient.api.searchRestaurant(
                            key = "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}",
                            longitude = longitude,
                            latitude = latitude,
                            page = page
                        )


                    if(response.isSuccessful) {

                        val places =
                            response.body()
                                ?.documents
                                ?: emptyList()


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


                val uniquePlaces =
                    allPlaces.distinctBy {
                        it.place_name
                    }


                Log.d(
                    "KAKAO_RESULT",
                    "중복 제거 후 : ${uniquePlaces.size}"
                )


                fetchUserProfileFromFirestore { profile ->

                    analyzeRestaurantsWithGemini(
                        uniquePlaces,
                        profile
                    )

                }


            } catch(e: Exception) {

                Log.e(
                    "SEARCH_RESTAURANT",
                    e.toString()
                )


                runOnUiThread {
                    showErrorScreen()
                }
            }
        }
    }
    private fun fetchUserProfileFromFirestore(
        onResult: (UserDietProfile) -> Unit
    ) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if(uid == null) {

            onResult(
                UserDietProfile(
                    emptyList(),
                    emptyList()
                )
            )

            return
        }


        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->


                val allergies =
                    doc.get("allergies") as? List<String>
                        ?: emptyList()


                val preferences =
                    doc.get("preferences") as? List<String>
                        ?: emptyList()


                onResult(
                    UserDietProfile(
                        allergies,
                        preferences
                    )
                )

            }
            .addOnFailureListener {

                onResult(
                    UserDietProfile(
                        emptyList(),
                        emptyList()
                    )
                )

            }
    }

    private suspend fun callGeminiWithRetry(
        apiCall: suspend () -> retrofit2.Response<GeminiResponse>
    ): retrofit2.Response<GeminiResponse> {

        repeat(3) { attempt ->
            val response = apiCall()

            if (response.isSuccessful) {
                return response
            }

            if (response.code() == 503 || response.code() == 429) {
                Log.d("GEMINI_RETRY", "${attempt + 1}번째 재시도")
                kotlinx.coroutines.delay(3000)
            } else {
                return response
            }
        }

        throw Exception("Gemini 서버 응답 실패")
    }

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
                val restaurant = Restaurant(
                    name = obj.getString("name"),
                    address = obj.getString("address"),
                    feature = obj.getString("feature"),
                    tags = tags
                )

                newList.add(restaurant)
            }

            Log.d("RESTAURANT_SIZE", "newList 개수 = ${newList.size}")

            withContext(Dispatchers.Main) {
                restaurantList.clear()
                restaurantList.addAll(newList)

                adapter.notifyDataSetChanged()

                layoutLoading.visibility = View.GONE
                layoutError.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

            }

            CoroutineScope(Dispatchers.IO).launch {

                newList.forEachIndexed { index, restaurant ->

                    val imageUrl = getRestaurantImageUrl(restaurant.name)

                    restaurant.imageUrl = imageUrl

                    withContext(Dispatchers.Main) {
                        adapter.notifyItemChanged(index)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("JSON_ERROR", e.message ?: "")

            withContext(Dispatchers.Main) {
                showErrorScreen()
            }
        }
    }

    private suspend fun getRestaurantImageUrl(
        restaurantName: String
    ): String? {

        return try {

            val response = GooglePlacesRetrofitClient.api.searchPlace(
                query = restaurantName,
                apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
            )

            val photoReference =
                response.results
                    .firstOrNull()
                    ?.photos
                    ?.firstOrNull()
                    ?.photo_reference


            if (photoReference != null) {

                "https://maps.googleapis.com/maps/api/place/photo" +
                        "?maxwidth=400" +
                        "&photo_reference=$photoReference" +
                        "&key=${BuildConfig.GOOGLE_MAPS_API_KEY}"

            } else {
                null
            }

        } catch (e: Exception) {

            Log.e(
                "GOOGLE_PLACE_ERROR",
                e.toString()
            )

            null
        }
    }

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
                    GeminiRetrofitClient.api.generate(
                        apiKey = geminiKey,
                        request = request
                    )
                }

                Log.d("GEMINI_HTTP", "code=${response.code()}")

                if (response.isSuccessful) {

                    val result = response.body()?.candidates
                        ?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "[]"

                    Log.d("GEMINI_RESULT", result)

                    updateRecyclerView(result)

                } else {

                    Log.e(
                        "GEMINI_ERROR",
                        "Gemini 응답 실패 code=${response.code()}"
                    )

                    withContext(Dispatchers.Main) {
                        showErrorScreen()
                    }
                }
            } catch (e: Exception) {
                Log.e("GEMINI_ERROR", e.toString())
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    showErrorScreen()
                }
            }
        }
    }

    private fun showErrorScreen() {

        layoutLoading.visibility = View.GONE
        recyclerView.visibility = View.GONE
        layoutError.visibility = View.VISIBLE
        isSearching = false
    }

}