package com.example.k_table.scan

import GeminiVisionRequest
import InlineData
import VisionContent
import VisionPart
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.example.k_table.R
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.util.Log
import com.example.k_table.api.GeminiRetrofitClient
import com.example.k_table.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScanLoadingFragment : Fragment(R.layout.fragment_scan_loading) {

    private lateinit var resultImage: ImageView
    private lateinit var scanLine: View

    // 애니메이션 관리 변수
    private var scanAnimator: ObjectAnimator? = null

    // Handler 관리 변수
    private val handler =
        Handler(Looper.getMainLooper())


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 하단 탭바 숨기기
        val bottomBar =
            activity?.findViewById<View>(R.id.bottomBar)

        bottomBar?.visibility = View.GONE

        resultImage = view.findViewById(R.id.resultImage)

        val controller =
            WindowCompat.getInsetsController(
                requireActivity().window,
                requireActivity().window.decorView
            )

        controller.hide(
            WindowInsetsCompat.Type.navigationBars()
        )

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        scanLine = view.findViewById(R.id.scanLine)

        // 전달받은 이미지
        val uriString = arguments?.getString("imageUri")

        uriString?.let {

            val uri = Uri.parse(it)

            // 화면에 이미지 표시
            resultImage.setImageURI(uri)

            // 메뉴 분석 시작
            /*CoroutineScope(Dispatchers.IO).launch {

                analyzeMenu(uri)

            }*/

            handler.postDelayed({

                val intent = Intent(
                    requireContext(),
                    ScanResultActivity::class.java
                )

                startActivity(intent)

            }, 2000)
        }

        startScanAnimation()

    }


    private fun startScanAnimation(){

        scanLine.post {

            val frame =
                requireView().findViewById<View>(R.id.scanFrame)

            val distance =
                frame.height - scanLine.height


            scanAnimator =
                ObjectAnimator.ofFloat(
                    scanLine,
                    "translationY",
                    0f,
                    distance.toFloat()
                ).apply {

                    duration = 1800

                    repeatCount =
                        ObjectAnimator.INFINITE

                    repeatMode =
                        ObjectAnimator.REVERSE

                    interpolator =
                        LinearInterpolator()

                    start()

                }

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()

        // 애니메이션 종료
        scanAnimator?.cancel()

        // Handler 종료
        handler.removeCallbacksAndMessages(null)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {

        val outputStream = ByteArrayOutputStream()

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            60,
            outputStream
        )

        val imageBytes = outputStream.toByteArray()

        Log.d(
            "GEMINI_SCAN",
            "image size=${imageBytes.size / 1024}KB, width=${bitmap.width}, height=${bitmap.height}"
        )

        return Base64.encodeToString(
            outputStream.toByteArray(),
            Base64.NO_WRAP
        )
    }

    private fun uriToBitmap(uri: Uri): Bitmap {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            val source =
                ImageDecoder.createSource(
                    requireContext().contentResolver,
                    uri
                )

            ImageDecoder.decodeBitmap(source)

        } else {

            MediaStore.Images.Media.getBitmap(
                requireContext().contentResolver,
                uri
            )

        }
    }

    private suspend fun analyzeMenu(uri: Uri) {

        val bitmap = uriToBitmap(uri)

        val base64 = bitmapToBase64(bitmap)

        val prompt = """
이 이미지는 음식점 메뉴판이다.
메뉴 이름만 추출해라.
가격은 제외.
설명은 제외.
JSON 배열만 출력한다.
예시

[
 "김치찌개",
 "된장찌개",
 "비빔밥"
]
""".trimIndent()

        val request = GeminiVisionRequest(

            contents = listOf(

                VisionContent(

                    parts = listOf(

                        VisionPart(text = prompt),

                        VisionPart(

                            inlineData = InlineData(
                                mimeType = "image/jpeg",
                                data = base64
                            )
                        )
                    )
                )
            )
        )

        Log.d("GEMINI_SCAN", "Vision 호출 시작")

        val response =
            GeminiRetrofitClient.api.generateVision(

                apiKey = BuildConfig.GEMINI_SCAN_API_KEY,
                request = request

            )
        Log.d("GEMINI_SCAN", "Vision 응답 코드: ${response.code()}")

        if(response.isSuccessful){

            val result =
                response.body()
                    ?.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text

            Log.d("MENU_RESULT", result ?: "null")
        }else{
            Log.e(
                "MENU_RESULT",
                "code=${response.code()} body=${response.errorBody()?.string()}"
            )
        }
    }
}