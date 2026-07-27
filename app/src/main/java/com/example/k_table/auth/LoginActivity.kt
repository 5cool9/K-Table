package com.example.k_table

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()

        // 이메일 로그인 버튼 클릭 시
        val btnEmailLogin = findViewById<Button>(R.id.btnEmailLogin)
        btnEmailLogin.setOnClickListener {
            navigateToMain()
        }

        // 구글 로그인 버튼 클릭 시
        val btnGoogleLogin = findViewById<LinearLayout>(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            Log.d("GOOGLE_TEST", "버튼 클릭")
            signInWithGoogle()
        }

        // 회원가입 버튼 클릭 시
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        tvSignUp.setOnClickListener {
            // TODO: 회원가입 화면으로 이동하는 코드 작성 예정
        }
    }

    private fun signInWithGoogle() {

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("625773282373-9f1k3nhr1ctamjj79ppmbof9cjni1oqs.apps.googleusercontent.com")
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(this)

        CoroutineScope(Dispatchers.Main).launch {

            try {
                Log.d("GOOGLE_TEST", "credential 호출 전")

                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )

                Log.d("GOOGLE_TEST", "credential 호출 성공")

                val credential = result.credential

                val googleCredential = com.google.android.libraries.identity.googleid
                    .GoogleIdTokenCredential
                    .createFrom(credential.data)

                val firebaseCredential = GoogleAuthProvider
                    .getCredential(
                        googleCredential.idToken,
                        null
                    )

                firebaseAuth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            checkUserProfile()

                        } else {
                            println("Firebase 로그인 실패: ${task.exception}")

                        }
                    }

            } catch (e: Exception) {
                Log.e("GOOGLE_LOGIN_ERROR", e.message.toString())

                Toast.makeText(
                    this@LoginActivity,
                    "등록된 Google 계정이 없습니다. 계정을 추가후 다시 시작해주세요.",
                    Toast.LENGTH_SHORT
                ).show()


                val intent = Intent(
                    android.provider.Settings.ACTION_ADD_ACCOUNT
                )

                intent.putExtra(
                    android.provider.Settings.EXTRA_ACCOUNT_TYPES,
                    arrayOf("com.google")
                )

                startActivity(intent)
            }
        }
    }

    private fun checkUserProfile() {

        val uid = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {
                    // 기존 회원
                    navigateToMain()
                } else {
                    // 신규 회원
                    navigateToPreference()
                }
            }
    }

    private fun navigateToPreference() {

        val intent = Intent(
            this,
            Preference2Activity::class.java
        )

        startActivity(intent)
        finish()
    }

    // 메인 화면(탭바가 있는 곳)으로 이동하고 로그인 화면은 종료하는 함수
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}