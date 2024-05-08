package com.example.traveltaleproject.tale;

import android.content.Context
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
    private var talesid: String? = null // 수정할 데이터의 고유 ID


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaleWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
        userId = getSessionId()
        travelListId = intent.getStringExtra("travelListId") ?: ""
        talesid = intent.getStringExtra("talesid") // 수정할 데이터의 고유 ID 가져오기
        databaseReference = FirebaseDatabase.getInstance().reference.child("TravelList").child(userId).child(travelListId)



        // 기존 데이터 가져오기
        fetchTravelListData()
        // taleText 가져오기
        if (talesid != null) {
            fetchTaleData(talesid!!)
        }

        binding.saveTaleButton.setOnClickListener {
            val taleText = binding.taleWrite.text.toString()
            if (taleText.isNotEmpty()) {
                if (talesid != null) {
                    // 수정 모드
                    updateTaleInDatabase(talesid!!, taleText)
                } else {
                    // 새로운 글 작성 모드
                    saveTaleToDatabase(taleText)
                }
            }
        }
    }
    private fun saveTaleToDatabase(taleText: String) {
        val newtalesid = generateUniqueId()
        val taleData = TaleData(talesid = newtalesid, text = taleText)
        databaseReference.child("tales").child(newtalesid).setValue(taleData)
            .addOnSuccessListener {
                val intent = Intent(this, TaleGetActivity::class.java)
                intent.putExtra("taleData", taleData)
                intent.putExtra("travelListId", travelListId)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "테일 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateTaleInDatabase(talesid: String, taleText: String) {
        val updatedTaleData = TaleData(talesid = talesid, text = taleText)
        databaseReference.child("tales").child(talesid).setValue(updatedTaleData)
            .addOnSuccessListener {
                val intent = Intent(this, TaleGetActivity::class.java)
                intent.putExtra("taleData", updatedTaleData)
                intent.putExtra("travelListId", travelListId)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "테일 수정 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun fetchTaleData(talesid: String) {
        databaseReference.child("tales").child(talesid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val taleData = snapshot.getValue(TaleData::class.java)
                if (taleData != null) {
                    // 가져온 데이터를 UI 요소에 표시
                    binding.taleWrite.setText(taleData.text)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun fetchTravelListData() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
                binding.mapTxt.setText(address)
                Picasso.get().load(travelImage).into(binding.mainImg)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun getSessionId(): String {
        return sharedPreferences.getString("user_id", "").toString()
    }
    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }
}
