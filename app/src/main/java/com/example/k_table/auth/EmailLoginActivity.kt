package com.example.k_table.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.k_table.MainActivity
import com.example.k_table.Preference2Activity
import com.example.k_table.databinding.ActivityEmailLoginBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.Toast
import com.example.k_table.R
import com.google.firebase.auth.FirebaseAuthException

class EmailLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmailLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()


        // 이메일 로그인
        binding.btnLogin.setOnClickListener {

            val email =
                binding.etEmail.text.toString().trim()

            val password =
                binding.etPassword.text.toString().trim()


            if(email.isEmpty() || password.isEmpty()){

                Toast.makeText(
                    this,
                    "이메일과 비밀번호를 입력해주세요.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            firebaseAuth
                .signInWithEmailAndPassword(
                    email,
                    password
                )
                .addOnSuccessListener {

                    moveToMain()

                }
                .addOnFailureListener { e ->

                    val message = when {

                        e.message?.contains("no user record", true) == true ->
                            "등록되지 않은 이메일입니다."

                        e.message?.contains("password is invalid", true) == true ->
                            "비밀번호가 올바르지 않습니다."

                        e.message?.contains("badly formatted", true) == true ->
                            "이메일 형식이 올바르지 않습니다."

                        else ->
                            "로그인에 실패했습니다. 다시 시도해주세요."

                    }
                    Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // 회원가입 이동
        binding.tvSignUp.setOnClickListener {

            val intent =
                Intent(
                    this,
                    SignupActivity::class.java
                )

            startActivity(intent)

        }

        // 비밀번호 찾기
        binding.tvForgotPassword.setOnClickListener {

            val email =
                binding.etEmail.text.toString().trim()

            if(email.isEmpty()){

                Toast.makeText(
                    this,
                    "이메일을 먼저 입력해주세요.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            firebaseAuth
                .sendPasswordResetEmail(email)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "비밀번호 재설정 메일을 보냈습니다.",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                .addOnFailureListener {

                    Toast.makeText(
                        this,
                        "메일 전송 실패: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                }
        }

        // 구글 로그인
        binding.btnGoogleLogin.setOnClickListener {

            signInWithGoogle()

        }

        val tvForgotPassword =
            findViewById<TextView>(R.id.tvForgotPassword)

        tvForgotPassword.setOnClickListener {

            val email =
                findViewById<EditText>(R.id.etEmail)
                    .text.toString()
                    .trim()


            if(email.isEmpty()) {

                Toast.makeText(
                    this,
                    "이메일을 먼저 입력해주세요.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }


            firebaseAuth
                .sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->

                    if(task.isSuccessful){

                        Toast.makeText(
                            this,
                            "비밀번호 재설정 메일을 보냈습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                    }else{

                        Toast.makeText(
                            this,
                            "메일 전송 실패: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    private fun moveToMain(){

        val intent =
            Intent(
                this,
                MainActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
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

    private fun navigateToMain() {

        val intent = Intent(
            this,
            MainActivity::class.java
        )

        startActivity(intent)
        finish()
    }

    private fun navigateToPreference() {

        val intent =
            Intent(
                this,
                Preference2Activity::class.java
            )

        startActivity(intent)
        finish()
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
                    context = this@EmailLoginActivity
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
                                this@EmailLoginActivity,
                                "로그인에 실패했습니다. 다시 시도해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

            } catch (e: NoCredentialException) {

                Log.e("GOOGLE_LOGIN_ERROR", "계정 없음: ${e.message}")

                Toast.makeText(
                    this@EmailLoginActivity,
                    "Google 로그인 정보를 가져올 수 없습니다. 계정 등록 또는 Google 서비스 상태를 확인해주세요.",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: GetCredentialCancellationException) {
                Log.d("GOOGLE_LOGIN_CANCEL", "사용자가 로그인을 취소함")

            } catch (e: GetCredentialInterruptedException) {
                // 일시적인 통신 중단일 경우
                Log.e("GOOGLE_LOGIN_ERROR", "일시적 오류: ${e.message}")
                Toast.makeText(
                    this@EmailLoginActivity,
                    "일시적인 오류가 발생했습니다. 다시 시도해주세요.",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: GetCredentialProviderConfigurationException) {
                // 기기 설정 자체에 문제가 있는 경우
                Log.e("GOOGLE_LOGIN_ERROR", "Provider 설정 오류: ${e.message}")
                Toast.makeText(
                    this@EmailLoginActivity,
                    "이 기기에서 Google 로그인을 사용할 수 없습니다. Play 스토어 업데이트를 확인해주세요.",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: GetCredentialException) {
                // 위에서 못 잡은 나머지 Credential 관련 예외
                Log.e("GOOGLE_LOGIN_ERROR", "타입: ${e.javaClass.simpleName}, 메시지: ${e.message}")
                Toast.makeText(
                    this@EmailLoginActivity,
                    "로그인 중 오류가 발생했습니다. (${e.javaClass.simpleName})",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                // 네트워크 등의 오류
                Log.e("GOOGLE_LOGIN_ERROR", "알 수 없는 오류: ${e.javaClass.simpleName}, ${e.message}")
                Toast.makeText(
                    this@EmailLoginActivity,
                    "알 수 없는 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}