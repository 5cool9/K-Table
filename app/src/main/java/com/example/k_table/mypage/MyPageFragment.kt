package com.example.k_table

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import android.view.View
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var btnEdit: MaterialButton
    private lateinit var btnLogout: TextView
    private lateinit var txtName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var layoutAllergy: LinearLayout
    private lateinit var layoutPreference: LinearLayout
    private lateinit var layoutLanguage: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtName = view.findViewById(R.id.txtName)
        txtEmail = view.findViewById(R.id.txtEmail)

        btnEdit = view.findViewById(R.id.btnEdit)
        btnLogout = view.findViewById(R.id.btnLogout)

        layoutAllergy = view.findViewById(R.id.layoutAllergy)
        layoutPreference = view.findViewById(R.id.layoutPreference)
        layoutLanguage = view.findViewById(R.id.layoutLanguage)


        layoutAllergy.setOnClickListener {

            val intent =
                Intent(
                    requireContext(),
                    Preference4Activity::class.java
                )

            intent.putExtra(
                "EDIT_MODE",
                true
            )

            startActivity(intent)
        }



        layoutPreference.setOnClickListener {

            val intent =
                Intent(
                    requireContext(),
                    Preference3Activity::class.java
                )

            intent.putExtra(
                "EDIT_MODE",
                true
            )

            startActivity(intent)
        }



        layoutLanguage.setOnClickListener {

            val intent =
                Intent(
                    requireContext(),
                    Preference2Activity::class.java
                )

            intent.putExtra(
                "EDIT_MODE",
                true
            )

            startActivity(intent)
        }



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
                Intent(
                    requireContext(),
                    LoginActivity::class.java
                )

            startActivity(intent)

            requireActivity().finish()

        }


        loadUserInfo()
    }



    private fun loadUserInfo() {

        val user =
            FirebaseAuth.getInstance().currentUser
                ?: return


        txtEmail.text =
            user.email



        val db =
            FirebaseFirestore.getInstance()


        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                if(document.exists()) {

                    val nickname =
                        document.getString("nickname")


                    txtName.text =
                        nickname ?: "User"
                }
            }
    }
}