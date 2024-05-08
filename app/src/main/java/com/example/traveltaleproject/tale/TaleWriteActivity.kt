package com.example.traveltaleproject.tale

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        binding = ActivityTaleWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 데이터 초기화 펑션 호출
        initializeData()

        // 상위 게시물에서 기존 정보 받아오는 평선 호출
        fetchTravelListData()

        // 수정을 위해 기존 텍스트 받아오는 펑션 호출
        fetchTaleData()

        // 레이아웃 저장버튼 바인딩 & 저장 펑션 호출
        binding.saveTaleButton.setOnClickListener {
            val taleText = binding.taleWrite.text.toString()
            if (taleText.isNotEmpty()) {
                saveTaleToDatabase(taleText)
            }
        }
    }

    //데이터 초기화 펑션
    private fun initializeData() {
        sharedPreferences = getSharedPreferences("MyInfo", MODE_PRIVATE)
        userId = getSessionId()
        travelListId = intent.getStringExtra("travelListId") ?: ""
        talesid = intent.getStringExtra("talesid")
        databaseReference = FirebaseDatabase.getInstance().reference.child("TravelList").child(userId).child(travelListId)
    }

    //상위 게시물에서 기존 정보 받아오는 평선
    private fun fetchTravelListData() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //상위 게시물에서 기존 정보 받아오는 상세 펑션 호출
                setTravelListData(snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // 수정을 위해 기존 텍스트 받아오는 펑션
    private fun fetchTaleData() {
        if (talesid != null) {
            databaseReference.child("tales").child(talesid!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val taleData = snapshot.getValue(TaleData::class.java)
                    taleData?.let { binding.taleWrite.setText(it.text) }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    // DB 저장 펑션
    private fun saveTaleToDatabase(taleText: String) {
        val newtalesid = generateUniqueId()
        val taleData = TaleData(talesid = newtalesid, text = taleText)
        databaseReference.child("tales").child(newtalesid).setValue(taleData)
            // 저장 성공 시 테일 상세 액티비티로 데이터를 넘기고 이동하는 펑션 호출
            .addOnSuccessListener { navigateToTaleGetActivity(taleData) }
            .addOnFailureListener { showToast("테일 저장 실패: ${it.message}") }
    }

    //상위 게시물에서 기존 정보 받아오는 상세 펑션
    private fun setTravelListData(snapshot: DataSnapshot) {
        val title = snapshot.child("title").value.toString()
        val startDate = snapshot.child("startDate").value as Long
        val endDate = snapshot.child("endDate").value as Long
        val address = snapshot.child("address").value.toString()
        val travelImage = snapshot.child("travelImage").value.toString()

        binding.taleWriteTitle.setText(title)
        binding.startDateTxt.text = formatDate(startDate)
        binding.endDateTxt.text = formatDate(endDate)
        binding.mapTxt.text = address
        Picasso.get().load(travelImage).into(binding.mainImg)
    }

    // 날짜 형식 지정 펑션
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH)
        return sdf.format(Date(timestamp))
    }

    // 사용자 정보 받아오는 펑션
    private fun getSessionId(): String {
        return sharedPreferences.getString("user_id", "").toString()
    }

    // 새로 작성하는 테일에 랜덤 ID를 부여하는 펑션
    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    // 테일 상세 액티비티로 데이터를 넘기고 이동하는 펑션
    private fun navigateToTaleGetActivity(taleData: TaleData) {
        val intent = Intent(this, TaleGetActivity::class.java)
        intent.putExtra("taleData", taleData)
        intent.putExtra("travelListId", travelListId)
        startActivity(intent)
        finish()
    }

    // 토스트 펑션
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
