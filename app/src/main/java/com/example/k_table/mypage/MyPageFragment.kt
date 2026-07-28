package com.example.k_table

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import android.view.View

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var btnEdit: MaterialButton
    private lateinit var btnLogout: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        btnEdit = view.findViewById(R.id.btnEdit)
        btnLogout = view.findViewById(R.id.btnLogout)


        btnEdit.setOnClickListener {

            Toast.makeText(
                requireContext(),
                "프로필 편집 화면",
                Toast.LENGTH_SHORT
            ).show()

        }


        btnLogout.setOnClickListener {

            Toast.makeText(
                requireContext(),
                "로그아웃 되었습니다.",
                Toast.LENGTH_SHORT
            ).show()


            val intent =
                Intent(requireContext(), LoginActivity::class.java)

            startActivity(intent)

            requireActivity().finish()

        }
    }
}