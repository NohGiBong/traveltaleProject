package com.example.traveltale

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import com.example.traveltale.databinding.ActivityRegisterBinding
import com.example.traveltale.user.Member
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var databaseReference: DatabaseReference
    private var isIdValid: Boolean = false
    private var isPwValid: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Realtime DB 초기화
        databaseReference = FirebaseDatabase.getInstance().reference.child("Member")

        // 중복확인 버튼 클릭 리스너
        binding.regiChkId.setOnClickListener {
            val id = binding.regiId.text.toString()
            checkDuplicatedId(id)
        }

        // 가입하기 버튼 클릭 리스너
        binding.regiBtn.setOnClickListener {
            if (isIdValid && isPwValid) {
                val nickname = binding.regiNickname.text.toString()
                val id = binding.regiId.text.toString()
                val pw = binding.regiPw.text.toString()
                val email =
                    binding.regiEmail.text.toString() + "@" + binding.regiEmailDomain.selectedItem.toString()
                val phone = binding.regiPhone.text.toString()

                // 회원 정보 객체 생성
                val member = Member(nickname, id, pw, email, phone)

                // Firebase Realtime DB 회원 정보 저장
                databaseReference.child(id).setValue(member)
                    .addOnSuccessListener {
                        showToast("회원가입 성공")

                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        showToast("회원가입 실패")
                    }
            } else {
                showToast("아이디 및 비밀번호를 확인해주세요")
            }
        }

        // 비밀번호 입력 필드 TextWatcher 추가 : 유효성 검사
        binding.regiPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isPasswordValid()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        // 비밀번호 확인 필드 TextWatcher 추가 :  유효성 검사
        binding.regiChkPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkPasswordConfirm()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        // Phone 입력 필드 TextWatcher 추가 : 자동 하이폰 추가
        val editTextPhone = binding.regiPhone
        editTextPhone.addTextChangedListener(PhoneTextWatcher(binding))

        //  Email Domain 필드 아이템 항목 색상 변경
        val domainItem = binding.regiEmailDomain
        val domainAdapter = CustomSpinnerAdapter(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.domain)
        )
        domainItem.adapter = domainAdapter
    }

    private fun checkDuplicatedId(id: String) {
        databaseReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val checkIdFail = binding.checkIdFail
                val checkIdSuccess = binding.checkIdSuccess
                val regiId = binding.regiId
                if (snapshot.exists()) {
                    checkIdFail.visibility = View.VISIBLE
                    checkIdSuccess.visibility = View.GONE
                    isIdValid = false

                    // 중복된 아이디일 경우 EditText 밑줄 빨강색으로 색상 변경
                    regiId.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@RegisterActivity,
                            R.color.fail
                        )
                    )
                } else {
                    checkIdFail.visibility = View.GONE
                    checkIdSuccess.visibility = View.VISIBLE
                    isIdValid = true

                    // 사용 가능한 아이디일 경우 EditText 밑줄 초록색으로 색상 변경
                    regiId.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@RegisterActivity,
                            R.color.success
                        )
                    )

                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("중복 확인 실패")
            }
        })
    }

    private fun isPasswordValid() {
        val password = binding.regiPw.text.toString().trim()
//        val pattern =
//            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-])[A-Za-z\\d!@#\$%^&*()_+\\-]{6,12}\$".toRegex()
        val pattern =
            "1".toRegex()
        val checkPwFail = binding.checkPwFail


        if (password.matches(pattern)) {
            checkPwFail.visibility = View.GONE
            binding.regiPw.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@RegisterActivity, R.color.success
                )
            )
        } else {
            checkPwFail.visibility = View.VISIBLE
            binding.regiPw.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@RegisterActivity, R.color.fail
                )
            )
        }
    }

    private fun checkPasswordConfirm() {
        val password = binding.regiPw.text.toString()
        val confirmPw = binding.regiChkPw.text.toString()

        if (password == confirmPw) {
            binding.regiChkPw.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@RegisterActivity, R.color.success
                )
            )
            isPwValid = true
        } else {
            binding.regiChkPw.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@RegisterActivity, R.color.fail
                )
            )
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    class CustomSpinnerAdapter(context: Context, resource: Int, objects: Array<String>) :
        ArrayAdapter<String>(context, resource, objects) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.setTextColor(Color.BLACK) // 텍스트 색상 변경
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.setTextColor(Color.BLACK) // 팝업 텍스트 색상 변경
            return view
        }

    }
}