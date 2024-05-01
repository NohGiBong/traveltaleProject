package com.example.traveltaleproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.user.Member
import com.example.traveltaleproject.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LoginActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var googleApiClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Realtime DB 및 Firebase Authentication 초기화
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("Member")

        // 구글 로그인 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
        googleApiClient.connect()

        // 로그인 버튼 클릭
        binding.loginBtn.setOnClickListener {
            val id = binding.loginId.text.toString()
            val pw = binding.loginPw.text.toString()

            if (id.isEmpty() || pw.isEmpty()) {
                showToast("아이디와 비밀번호를 입력해주세요")
            } else {
                dbLogin(id, pw)
            }
        }

        // 회원가입 버튼 클릭
        binding.regiBtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


        // 구글 로그인 버튼 클릭
        binding.loginGoogle.setOnClickListener {
            googleSignIn()
        }

        // 네이버 로그인 버튼 클릭
        binding.loginNaver.setOnClickListener {

        }
    }

    // Realtime DB 정보 가져와서 로그인
    private fun dbLogin(id: String, pw: String) {
        // Realtime DB에 입력된 아이디에 해당하는 사용자 정보 조회
        databaseReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(Member::class.java)
                    if (user?.pw == pw) {
                        showToast("로그인 성공 🐥")
                        // 로그인 성공 후 다음 화면 작성
                    } else {
                        showToast("비밀번호가 일치하지 않습니다.")
                    }
                } else {
                    showToast("존재하지 않는 아이디입니다.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("로그인 오류 : ${error.message}")
            }
        })
    }

    // 구글 로그인 처리
    private fun googleSignIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // onActivityResult 오버라이드
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            if (result != null) {
                if (result.isSuccess) {
                    val account = result.signInAccount
                    firebaseAuthWithGoogle(account)
                } else {
                    showToast("구글 로그인 실패")
                }
            }
        }
    }

    // Firebase 인증을 통한 구글 로그인 처리
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userId = user.uid
                        val email = user.email
                        val userName = user.displayName
                        // 구글 로그인한 유저의 정보를 Realtime Database 저장
                        val newUser = Member(userName, userId, "", email, "")
                        databaseReference.child(userId).setValue(newUser)
                        showToast("구글 로그인 성공")
                        // 로그인 후 처리
                    }
                } else {
                    showToast("구글 로그인 실패")
                }
            }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    data class User(
        val name: String?,
        val email: String?
    )

    companion object {
        private const val RC_SIGN_IN = 100
    }
}