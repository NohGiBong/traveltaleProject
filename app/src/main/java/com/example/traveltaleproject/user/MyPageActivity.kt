package com.example.traveltaleproject.user

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.LoginActivity
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityMypageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK


class MyPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMypageBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)

        // GoogleSignInClient 초기화
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)

        // Firebase Realtime Database 초기화
        val databaseReference = FirebaseDatabase.getInstance().reference

        // 사용자 정보 가져오기
        val userId = sharedPreferences.getString("user_id", "")
        showToast("$userId")
        if (!userId.isNullOrEmpty()) {
            getNickNameFromDB(userId, databaseReference)
        }

        // 로그아웃 버튼 클릭
        binding.mypageLogout.setOnClickListener {
            confirmLogout()
        }

        // 개인 정보 관리 버튼 클릭
        binding.myinfoBtn.setOnClickListener {
            val intent = Intent(this, MyInfoActivity::class.java)
            val userId = sharedPreferences.getString("user_id", "")
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }
    }

    private fun confirmLogout() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("로그아웃")
            .setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
                loginTypeConfirm()
            }
            .setNegativeButton("취소") { dialogInterface: DialogInterface, i: Int ->
                // 취소 버튼 클릭 시 아무 동작 없음
            }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(R.color.black))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.black))
        }

        dialog.show()
    }

    private fun loginTypeConfirm() {
        val loginType = sharedPreferences.getString("login_type", "")

        when (loginType) {
            "normal" -> {
                normalLogout()
            }

            "naver" -> {
                naverLogout()
            }

            "google" -> {
                googleLogout()
            }

            "kakao" -> {
                kakaoLogout()
            }

            else -> { showToast("LOGIN TYPE 식별 불가")}
        }
    }

    private fun normalLogout() {
        deleteSession()
        moveToLoginActivity()
        showToast("로그아웃하였습니다 🐳")
    }

    private fun naverLogout() {
        NaverIdLoginSDK.logout()
        deleteSession()
        moveToLoginActivity()
        showToast("로그아웃하였습니다 🐳")
    }

    private fun kakaoLogout() {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("TAG", "카카오 로그아웃 실패", error)
            } else {
                deleteSession()
                moveToLoginActivity()
                showToast("로그아웃하였습니다 🐳")
            }
        }
    }

    private fun googleLogout() {
        googleSignInClient.signOut().addOnCompleteListener {
            if (it.isSuccessful) {
                deleteSession()
                moveToLoginActivity()
                showToast("로그아웃하였습니다 🐳")
            } else {
                // 로그아웃 실패 처리
            }
        }
    }

    private fun deleteSession() {
        val editor = sharedPreferences.edit()
        editor.remove("user_id")
        editor.remove("login_type")
        editor.apply()
    }

    private fun moveToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getNickNameFromDB(userId: String, databaseReference: DatabaseReference) {
        val userRef = databaseReference.child("Member").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Member::class.java)
                // 사용자 정보가 null이 아닌 경우에만 닉네임 설정
                if (user != null) {
                    val userNickname = user.nickname
                    if (!userNickname.isNullOrEmpty()) {
                        binding.mypageNickname.text = userNickname
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("dbError", "DB에서 가져오지 못함")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

