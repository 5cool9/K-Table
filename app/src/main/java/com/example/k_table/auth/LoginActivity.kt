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
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
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
            .setFilterByAuthorizedAccounts(true)
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
                    .getCredential(googleCredential.idToken, null)


                firebaseAuth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            checkUserProfile()

                        } else {
                            Log.e("GOOGLE_LOGIN_ERROR", "Firebase 인증 실패: ${task.exception}")
                            Toast.makeText(
                                this@LoginActivity,
                                "로그인에 실패했습니다. 다시 시도해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

            } catch (e: NoCredentialException) {
                // 진짜로 기기에 등록된 Google 계정이 없는 경우에만 계정 추가 화면으로 이동
                Log.e("GOOGLE_LOGIN_ERROR", "계정 없음: ${e.message}")

                Toast.makeText(
                    this@LoginActivity,
                    "Google 로그인 정보를 가져올 수 없습니다. 계정 등록 또는 Google 서비스 상태를 확인해주세요.",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: GetCredentialCancellationException) {
                Log.d("GOOGLE_LOGIN_CANCEL", "사용자가 로그인을 취소함")

            } catch (e: GetCredentialInterruptedException) {
                // 일시적인 통신 중단일 경우
                Log.e("GOOGLE_LOGIN_ERROR", "일시적 오류: ${e.message}")
                Toast.makeText(
                    this@LoginActivity,
                    "일시적인 오류가 발생했습니다. 다시 시도해주세요.",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: GetCredentialProviderConfigurationException) {
                // 기기 설정 자체에 문제가 있는 경우
                Log.e("GOOGLE_LOGIN_ERROR", "Provider 설정 오류: ${e.message}")
                Toast.makeText(
                    this@LoginActivity,
                    "이 기기에서 Google 로그인을 사용할 수 없습니다. Play 스토어 업데이트를 확인해주세요.",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: GetCredentialException) {
                // 위에서 못 잡은 나머지 Credential 관련 예외
                Log.e("GOOGLE_LOGIN_ERROR", "타입: ${e.javaClass.simpleName}, 메시지: ${e.message}")
                Toast.makeText(
                    this@LoginActivity,
                    "로그인 중 오류가 발생했습니다. (${e.javaClass.simpleName})",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                // 네트워크 등의 오류
                Log.e("GOOGLE_LOGIN_ERROR", "알 수 없는 오류: ${e.javaClass.simpleName}, ${e.message}")
                Toast.makeText(
                    this@LoginActivity,
                    "알 수 없는 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
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