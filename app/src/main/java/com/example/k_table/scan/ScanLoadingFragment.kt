package com.example.k_table.scan

import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.example.k_table.R

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
            resultImage.setImageURI(Uri.parse(it))
        }

        startScanAnimation()

        // 3초 후 결과 화면
        handler.postDelayed({

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    ScanResultFragment()
                )
                .commit()

        }, 100000)

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

}