package com.example.traveltaleproject.user

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.traveltaleproject.PhoneTextWatcher
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityMyinfoBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyinfoBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private var isPwValid: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().reference
        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)

        val userId = intent.getStringExtra("user_id")

        userId?.let { fetchUserInfo(it) }

        binding.myinfoUpdateBtn.setOnClickListener {
            if(isPwValid) {
                updateMemberInfo()
            }
            else {
                showToast("비밀번호를 확인해주세요")
            }
        }

        // 비밀번호 입력 필드 TextWatcher 추가 : 유효성 검사
        binding.myinfoPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.myinfoPw.isFocused) {
                    isPasswordValid()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        // 비밀번호 확인 필드 TextWatcher 추가 :  유효성 검사
        binding.myinfoChkPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.myinfoChkPw.isFocused) {
                    checkPasswordConfirm()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        // Phone 입력 필드 TextWatcher 추가 : 자동 하이폰 추가
        binding.myinfoPhone.addTextChangedListener(PhoneTextWatcher(binding))
    }

    private fun fetchUserInfo(userId: String) {
        val userRef = databaseReference.child("Member").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Member::class.java)
                user?.let {
                    val loginType = sharedPreferences.getString("login_type", "")

                    // 로그인 유형에 따라 수정 가능 필드 설정
                    setupEditableFields(loginType ?: "")

                    // 사용자 정보를 가져와서 화면에 표시
                    binding.myinfoNickname.setText(it.nickname)
                    binding.myinfoId.setText(it.id)
                    binding.myinfoPhone.setText(it.phone)
                    binding.myinfoPw.setText(it.pw)
                    binding.myinfoChkPw.setText(it.pw)

                    // 소셜 로그인은 비밀번호를 저장 안하기 때문에 뷰단에 임의로 * 표시
                    if (it.pw.isNullOrEmpty()) {
                        binding.myinfoPw.setText("**********")
                        binding.myinfoChkPw.setText("**********")

                        // 소셜 로그인은 임의로 * 표시 했기에 비밀번호 유효성 검사 true로 할당해줌
                        isPwValid = true
                    }

                    val email = it.email
                    if (!email.isNullOrEmpty()) {
                        // '@'를 기준으로 이메일 아이디와 도메인으로 분할
                        val atIndex = email.indexOf('@')
                        if (atIndex != -1) {
                            val emailId = email.substring(0, atIndex)
                            val domain = email.substring(atIndex + 1)

                            // 이메일 아이디를 EditText에 설정
                            binding.myinfoEmail.setText(emailId)

                            // 도메인을 Spinner에서 선택
                            val domainIndex = getDomainIndex(domain)
                            if (domainIndex != -1) {
                                if (loginType == "google") {
                                    val googleDomainIndex = getDomainIndex("gmail.com")
                                    if (googleDomainIndex != -1) {
                                        binding.myinfoEmailDomain.setSelection(googleDomainIndex)
                                    }
                                } else {
                                    binding.myinfoEmailDomain.setSelection(domainIndex)
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("DB 값 들고오기 실패")
            }
        })
    }

    private fun updateMemberInfo() {
        val userId = intent.getStringExtra("user_id")
        val newNickname = binding.myinfoNickname.text.toString()
        val newId = binding.myinfoId.text.toString()
        val newPhone = binding.myinfoPhone.text.toString()
        val newPassword = binding.myinfoPw.text.toString()
        val newEmail = "${binding.myinfoEmail.text}@${binding.myinfoEmailDomain.selectedItem}"
        val loginType = sharedPreferences.getString("login_type", "")

        if (userId != null) {
            when (loginType) {
                "normal" -> {
                    updateUserInfo(userId, newNickname, newId, newPhone, newPassword, newEmail)
                }

                "kakao", "google" -> {
                    updateSocialUserInfo(userId, newNickname, newPhone)
                }

                "naver" -> {
                    updateNaverUserInfo(userId, newNickname)
                }

                else -> {
                    showToast("LOGIN TYPE 식별 불가")
                }
            }
        } else {
            showToast("사용자 ID를 가져올 수 없습니다.")
        }
    }

    private fun updateUserInfo(
        userId: String,
        newNickname: String,
        newId: String,
        newPhone: String,
        newPassword: String,
        newEmail: String
    ) {
        val userRef = databaseReference.child("Member").child(userId)

        val updatedUser = mutableMapOf<String, Any>(
            "nickname" to newNickname,
            "id" to newId,
            "pw" to newPassword,
            "phone" to newPhone,
            "email" to newEmail
        )

        userRef.updateChildren(updatedUser)
            .addOnSuccessListener {
                showToast("사용자 정보가 업데이트되었습니다.")
                moveToMyPageActivity()
            }
            .addOnFailureListener { e ->
                showToast("사용자 정보 업데이트 실패: ${e.message}")
            }
    }

    private fun updateSocialUserInfo(userId: String, newNickname: String, newPhone: String) {
        val userRef = databaseReference.child("Member").child(userId)

        val updatedUser = mapOf<String, Any>(
            "nickname" to newNickname,
            "phone" to newPhone
        )

        userRef.updateChildren(updatedUser)
            .addOnSuccessListener {
                showToast("사용자 정보가 업데이트되었습니다.")
                moveToMyPageActivity()
            }
            .addOnFailureListener { e ->
                showToast("사용자 정보 업데이트 실패: ${e.message}")
            }
    }

    private fun updateNaverUserInfo(userId: String, newNickname: String) {
        val userRef = databaseReference.child("Member").child(userId)

        val updatedUser = mapOf<String, Any>(
            "nickname" to newNickname
        )

        userRef.updateChildren(updatedUser)
            .addOnSuccessListener {
                showToast("사용자 정보가 업데이트되었습니다.")
                moveToMyPageActivity()
            }
            .addOnFailureListener { e ->
                showToast("사용자 정보 업데이트 실패: ${e.message}")
            }
    }

    // Login 유형 별 필드 활성화 로직
    private fun setupEditableFields(loginType: String) {
        when (loginType) {
            "normal" -> {
                setEditTextEditable(true)
            }

            "kakao", "google" -> {
                setEditTextEditable(false)
                binding.myinfoNickname.isEnabled = true
                binding.myinfoPhone.isEnabled = true
            }

            "naver" -> {
                setEditTextEditable(false)
                binding.myinfoNickname.isEnabled = true
            }

            else -> {
                showToast("LOGIN TYPE 식별 불가")
            }
        }
    }

    // EditText 활성화 로직
    private fun setEditTextEditable(editable: Boolean) {
        binding.myinfoNickname.isEnabled = editable
        binding.myinfoId.isEnabled = editable
        binding.myinfoPw.isEnabled = editable
        binding.myinfoChkPw.isEnabled = editable
        binding.myinfoEmail.isEnabled = editable
        binding.myinfoEmailDomain.isEnabled = editable
        binding.myinfoPhone.isEnabled = editable
    }

    // Spinner Domain Index 가져오기
    private fun getDomainIndex(domain: String): Int {
        val domainList = resources.getStringArray(R.array.domain)

        return domainList.indexOf(domain)
    }

    private fun isPasswordValid() {
        val password = binding.myinfoPw.text.toString().trim()
//        val pattern =
//            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-])[A-Za-z\\d!@#\$%^&*()_+\\-]{6,12}\$".toRegex()
        val pattern =
            "1".toRegex()
        val checkPwFail = binding.checkPwFail


        if (password.matches(pattern)) {
            checkPwFail.visibility = View.GONE
            binding.myinfoPw.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@MyInfoActivity, R.color.success
                )
            )
        } else {
            checkPwFail.visibility = View.VISIBLE
            binding.myinfoPw.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@MyInfoActivity, R.color.fail
                )
            )
        }
    }

    private fun checkPasswordConfirm() {
        val password = binding.myinfoPw.text.toString()
        val confirmPw = binding.myinfoChkPw.text.toString()

        if (password == confirmPw) {
            binding.myinfoChkPw.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@MyInfoActivity, R.color.success
                )
            )
            isPwValid = true
        } else {
            binding.myinfoChkPw.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@MyInfoActivity, R.color.fail
                )
            )
            isPwValid = false
        }
    }

    private fun moveToMyPageActivity() {
        val intent = Intent(this, MyPageActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
