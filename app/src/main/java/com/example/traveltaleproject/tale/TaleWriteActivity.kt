package com.example.traveltaleproject.tale

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.GetActivity
import com.example.traveltaleproject.databinding.ActivityTaleWriteBinding
import com.example.traveltaleproject.models.TaleData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TaleWriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaleWriteBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String
    private lateinit var travelListId: String
    private var talesid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 주요 기능 호출
        setupUI()
        initializeFirebase()
        loadData()

        binding.backBtn.setOnClickListener {
            val intent = Intent(this, GetActivity::class.java)
            intent.putExtra("travelListId", travelListId)
            startActivity(intent)
        }
    }

    // UI 데이터 받아오기
    private fun setupUI() {
        binding = ActivityTaleWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("MyInfo", MODE_PRIVATE)
        userId = getSessionId()
        travelListId = intent.getStringExtra("travelListId") ?: ""
        talesid = intent.getStringExtra("talesid")

        binding.saveTaleButton.setOnClickListener {
            val taleText = binding.taleWrite.text.toString()
            if (taleText.isNotEmpty()) {
                val operation = if (talesid != null) ::updateTaleInDatabase else ::saveTaleToDatabase
                operation(taleText)
            }
        }
    }

    // DB 초기화
    private fun initializeFirebase() {
        databaseReference = FirebaseDatabase.getInstance().reference
            .child("TravelList").child(userId).child(travelListId)
    }

    // 상위 게시물 불러오기 호출 후 현재 테일에 데이터가 있다면 현재 게시물 불러오기도 호출
    private fun loadData() {
        fetchTravelListData()
        talesid?.let { fetchTaleData(it) }
    }

    // DB에 저장
    private fun saveTaleToDatabase(taleText: String) {
        val newTalesId = generateUniqueId()
        val taleData = TaleData(talesid = newTalesId, text = taleText)
        persistTaleData(newTalesId, taleData)
    }

    // DB에 수정 저장
    private fun updateTaleInDatabase(taleText: String) {
        talesid?.let {
            val taleData = TaleData(talesid = it, text = taleText)
            persistTaleData(it, taleData)
        }
    }

    // 데이터 영속성 관리
    private fun persistTaleData(talesId: String, taleData: TaleData) {
        databaseReference.child("tales").child(talesId).setValue(taleData)
            .addOnSuccessListener { navigateToTaleGetActivity(taleData) }
            .addOnFailureListener { e ->
                Toast.makeText(this, "테일 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 저장 성공 시 데이터 전달 및 상세 페이지 이동
    private fun navigateToTaleGetActivity(taleData: TaleData) {
        Intent(this, TaleGetActivity::class.java).also {
            it.putExtra("taleData", taleData)
            it.putExtra("travelListId", travelListId)
            startActivity(it)
            finish()
        }
    }

    // 현재 게시물 데이터 불러오기
    private fun fetchTaleData(talesId: String) {
        databaseReference.child("tales").child(talesId)
            .addListenerForSingleValueEvent(valueEventListener { snapshot ->
                val taleData = snapshot.getValue(TaleData::class.java)
                taleData?.let { binding.taleWrite.setText(it.text) }
            })
    }

    // 상위 게시물 데이터 불러오기 및 UI 업데이트
    private fun fetchTravelListData() {
        databaseReference.addListenerForSingleValueEvent(valueEventListener { snapshot ->
            val title = snapshot.child("title").value.toString()
            val startDate = snapshot.child("startDate").value as Long
            val endDate = snapshot.child("endDate").value as Long
            val address = snapshot.child("address").value.toString()
            val travelImage = snapshot.child("travelImage").value.toString()

            binding.taleWriteTitle.setText(title)

            val sdf = SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH)
            val formattedStartDate = sdf.format(Date(startDate))
            val formattedEndDate = sdf.format(Date(endDate))

            binding.startDateTxt.text = formattedStartDate
            binding.endDateTxt.text = formattedEndDate
            binding.mapTxt.text = address

            Picasso.get().load(travelImage)

        })
    }

    // 사용자 정보 획득
    private fun getSessionId() = sharedPreferences.getString("user_id", "")!!

    // 테일 고유 ID 생성
    private fun generateUniqueId() = UUID.randomUUID().toString()

    // 데이터 불러오기 리스너
    private fun valueEventListener(onDataChangeHandler: (DataSnapshot) -> Unit): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) = onDataChangeHandler(snapshot)
            override fun onCancelled(error: DatabaseError) {}
        }
    }
}