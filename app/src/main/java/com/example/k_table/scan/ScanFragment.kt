package com.example.k_table.scan

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.k_table.R
import android.graphics.Color
import androidx.camera.view.PreviewView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import android.net.Uri
import android.util.Log
import android.widget.ImageButton


class ScanFragment : Fragment(R.layout.fragment_scan) {

    private lateinit var previewView: PreviewView

    // 갤러리 이미지 선택
    private val galleryLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri != null) {

                cameraProvider?.unbindAll()

                val fragment = ScanLoadingFragment()

                fragment.arguments = Bundle().apply {
                    putString("imageUri", uri.toString())
                }

                requireActivity()
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()

            }

        }

    // CameraX 관리 변수
    private var cameraProvider: ProcessCameraProvider? = null


    // 최신 권한 요청 방식
    private val cameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {

                startCamera()

            }

        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 하단 탭바 숨기기
        val bottomBar = activity?.findViewById<View>(R.id.bottomBar)
        bottomBar?.visibility = View.GONE


        previewView =
            view.findViewById(R.id.previewView)

        val galleryButton =
            view.findViewById<ImageButton>(R.id.btnGallery)


        galleryButton.setOnClickListener {

            Log.d(
                "GALLERY_TEST",
                "갤러리 클릭됨"
            )

            galleryLauncher.launch("image/*")

        }

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {

            startCamera()

        } else {

            // 수정 1. requestPermissions 제거
            cameraPermissionLauncher.launch(
                android.Manifest.permission.CAMERA
            )

        }


        // fragmentContainer 제약 수정 (꼬임 방지)
        val container = activity?.findViewById<View>(R.id.fragmentContainer)

        container?.let {

            val params =
                it.layoutParams as ConstraintLayout.LayoutParams

            params.bottomToTop =
                ConstraintLayout.LayoutParams.UNSET

            params.bottomToBottom =
                ConstraintLayout.LayoutParams.PARENT_ID

            it.layoutParams = params

        }


        activity?.window?.navigationBarColor =
            Color.parseColor("#222222")


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

    }


    override fun onDestroyView() {
        super.onDestroyView()


        // CameraX 종료
        cameraProvider?.unbindAll()


        val bottomBar = activity?.findViewById<View>(R.id.bottomBar)


        // fragmentContainer 제약 원래대로 복구
        val container = activity?.findViewById<View>(R.id.fragmentContainer)

        container?.let {

            val params =
                it.layoutParams as ConstraintLayout.LayoutParams


            params.bottomToBottom =
                ConstraintLayout.LayoutParams.UNSET


            params.bottomToTop =
                R.id.bottomBar


            it.layoutParams = params

        }

        // 시스템 UI 복구
        activity?.window?.navigationBarColor =
            Color.parseColor("#000000")

        val controller =
            WindowCompat.getInsetsController(
                requireActivity().window,
                requireActivity().window.decorView
            )

        controller.show(
            WindowInsetsCompat.Type.navigationBars()
        )

    }

    private fun startCamera(){

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({

            // 전역 변수에 저장
            cameraProvider =
                cameraProviderFuture.get()

            val preview =
                Preview.Builder()
                    .build()
                    .also {

                        it.setSurfaceProvider(
                            previewView.surfaceProvider
                        )

                    }

            val cameraSelector =
                CameraSelector.DEFAULT_BACK_CAMERA

            try {

                cameraProvider?.unbindAll()


                // viewLifecycleOwner 유지
                cameraProvider?.bindToLifecycle(

                    viewLifecycleOwner,

                    cameraSelector,

                    preview

                )

            } catch(e: Exception){

                e.printStackTrace()

            }

        },
            ContextCompat.getMainExecutor(requireContext()))

    }

}