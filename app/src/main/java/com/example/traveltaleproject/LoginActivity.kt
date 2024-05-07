package com.example.traveltaleproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.databinding.ActivityLoginBinding
import com.example.traveltaleproject.models.Member
import com.example.traveltaleproject.travellist.TravelListActivity
import com.example.traveltaleproject.user.MyPageActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import java.util.UUID

class LoginActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)

        // Firebase Realtime DB 및 Firebase Authentication 초기화
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("Member")

        // kakao SDK 초기화
        KakaoSdk.init(this@LoginActivity, getString(R.string.kakao_app_key))

        // naver SDK 초기화
        NaverIdLoginSDK.initialize(
            this@LoginActivity,
            getString(R.string.naver_client_id),
            getString(R.string.naver_client_secret),
            getString(R.string.naver_client_name)
        )

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
            naverSignIn()
        }

        // 카카오 로그인 버튼 클릭
        binding.loginKakao.setOnClickListener {
            kakaoSignIn()
        }
    }

    // Realtime DB에서 정보 가져와서 로그인
    private fun dbLogin(id: String, pw: String) {
        databaseReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(Member::class.java)
                    if (user?.pw == pw) {
                        showToast("로그인 성공 🐥")
                        saveSession(user)
                        startListActivity()
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

    // FirebaseUser를 Member로 변환하는 함수
    private fun convertFirebaseUserToMember(firebaseUser: FirebaseUser?): Member? {
        if (firebaseUser != null) {
            val userId = firebaseUser.uid
            val email = firebaseUser.email
            val userName = firebaseUser.displayName

            val newUser = Member(userName, userId, "", email, "", "google", "")
            databaseReference.child(userId).setValue(newUser)
            return newUser
        }
        return null
    }

    // Firebase 인증을 통한 구글 로그인 처리
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        // 구글 API를 통한 사용자 정보 가져오기
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val member = convertFirebaseUserToMember(user)
                        if (member != null) {
                            // 세션 저장
                            showToast("구글 로그인 성공")
                            saveSession(member)
                            startListActivity()
                        }
                    }
                } else {
                    showToast("구글 로그인 실패")
                }
            }
    }

    // 네이버 로그인 처리
    private fun naverSignIn() {
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                // 네이버 API를 통해 사용자 정보 가져오기
                NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
                    override fun onSuccess(result: NidProfileResponse) {
                        val name = result.profile?.name.toString()
                        val email = result.profile?.email.toString()
                        val mobile = result.profile?.mobile.toString()
                        val id = result.profile?.id

                        val newUser = Member(name, id, "", email, mobile, "naver", "")
                        if (id != null) {
                            databaseReference.child(id).setValue(newUser)
                        }

                        showToast("네이버 로그인 성공")
                        saveSession(newUser)
                        startListActivity()
                    }

                    override fun onError(errorCode: Int, message: String) {
                    }

                    override fun onFailure(httpStatus: Int, message: String) {
                    }
                })
            }

            override fun onError(errorCode: Int, message: String) {
                val accessToken = NaverIdLoginSDK.getAccessToken()
            }

            override fun onFailure(httpStatus: Int, message: String) {

            }
        }
        NaverIdLoginSDK.authenticate(this, oauthLoginCallback)
    }

    // 카카오 로그인 처리
    private fun kakaoSignIn() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("TAG", "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i("TAG", "카카오계정으로 로그인 성공 ${token.accessToken}")

                val userId = generateRandomUserId()

                // 카카오 API를 통해 사용자 정보 가져오기
                UserApiClient.instance.me { user, error ->
                    if (error != null) {
                        showToast("카카오 사용자 정보 가져오기 실패")
                    } else if (user != null) {
                        val nickname = user.kakaoAccount?.profile?.nickname.toString()
                        val email = user.kakaoAccount?.email.toString()


                        checkAndSignIn(email, userId, nickname, "kakao")
                    }
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    showToast("카카오톡 로그인 실패 !")
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    showToast("카카오톡 로그인 성공" + token.accessToken)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    // 중복된 이메일 여부 확인 및 로그인 처리
    private fun checkAndSignIn(
        email: String,
        userId: String,
        nickname: String,
        loginType: String
    ) {
        // 이메일이 이미 등록되어 있는지 확인
        val query = databaseReference.orderByChild("email").equalTo(email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isEmailExist = false
                for (data in snapshot.children) {
                    val user = data.getValue(Member::class.java)
                    if (user != null && user.logintype == loginType) {
                        // 이미 등록된 이메일이고 로그인 타입이 일치하는 경우
                        isEmailExist = true
                        showToast("로그인 성공")
                        saveSession(user)
                        startListActivity()
                        break
                    }
                }
                if (!isEmailExist) {
                    registerAndSignIn(email, userId, nickname, loginType)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("이메일 중복 확인 오류 : ${error.message}")
            }
        })
    }

    private fun registerAndSignIn(
        email: String,
        userId: String,
        nickname: String,
        loginType: String
    ) {
        // 사용자 정보를 DB에 등록하는 코드
        val newUser = Member(
            nickname,
            userId,
            "",
            email,
            "",
            loginType,
            ""
        )
        databaseReference.child(userId).setValue(newUser)

        saveSession(newUser)
        showToast("카카오 로그인 성공")
        startMyPageActivity() // 예시: 마이페이지 액티비티로 이동
    }


    // 사용자 세션 저장
    private fun saveSession(user: Member?) {
        val editor = sharedPreferences.edit()
        editor.putString("user_id", user?.id)
        editor.putString("login_type", user?.logintype)
        editor.apply()
    }

    // MyPageActivity로 화면을 전환하는 메서드
    private fun startMyPageActivity() {
        val intent = Intent(this, MyPageActivity::class.java)
        startActivity(intent)
        finish() // LoginActivity 종료
    }

    private fun startListActivity() {
        val intent = Intent(this, TravelListActivity::class.java)
        startActivity(intent)
        finish()
    }

    // uuid 생성 : 카카오는 id를 제공해주지 않음
    private fun generateRandomUserId(): String {
        return UUID.randomUUID().toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val RC_SIGN_IN = 100
    }
}
