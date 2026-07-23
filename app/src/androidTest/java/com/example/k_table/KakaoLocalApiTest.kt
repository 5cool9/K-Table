package com.example.k_table

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KakaoLocalApiTest {

    private val client = OkHttpClient()

    @Test
    fun searchCafeNearGangnamStation() {
        assertTrue(
            "REST API 키를 입력해 주세요.",
            KAKAO_REST_API_KEY.isNotBlank()
        )

        // 강남역 좌표
        val longitude = 127.0276
        val latitude = 37.4979

        val url = KEYWORD_SEARCH_URL
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter("query", "카페")
            .addQueryParameter("x", longitude.toString())
            .addQueryParameter("y", latitude.toString())
            .addQueryParameter("radius", "1000")
            .addQueryParameter("sort", "distance")
            .addQueryParameter("size", "15")
            .build()

        val request = Request.Builder()
            .url(url)
            .header(
                "Authorization",
                "KakaoAK $KAKAO_REST_API_KEY"
            )
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()

            Log.d(TAG, "응답 코드: ${response.code}")
            Log.d(TAG, "전체 응답: $responseBody")

            assertTrue(
                """
                카카오 API 호출 실패
                HTTP 상태: ${response.code}
                응답: $responseBody
                """.trimIndent(),
                response.isSuccessful
            )

            val json = JSONObject(responseBody)
            val places = json.getJSONArray("documents")

            Log.d(TAG, "검색 결과: ${places.length()}개")

            for (index in 0 until places.length()) {
                val place = places.getJSONObject(index)

                Log.d(
                    TAG,
                    """
                    ------------------------------
                    ${index + 1}. ${place.optString("place_name")}
                    카테고리: ${place.optString("category_name")}
                    주소: ${place.optString("road_address_name")}
                    거리: ${place.optString("distance")}m
                    경도: ${place.optString("x")}
                    위도: ${place.optString("y")}
                    카카오맵: ${place.optString("place_url")}
                    """.trimIndent()
                )
            }

            assertTrue(
                "주변에서 검색된 카페가 없습니다.",
                places.length() > 0
            )
        }
    }

    companion object {
        private const val TAG = "KakaoLocalApiTest"

        private const val KEYWORD_SEARCH_URL =
            "https://dapi.kakao.com/v2/local/search/keyword.json"

        // 카카오 디벨로퍼스의 REST API 키만 입력
        private const val KAKAO_REST_API_KEY =
            BuildConfig.KAKAO_REST_API_KEY
    }
}