package com.example.traveltaleproject.tale

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import java.util.UUID

class TaleWriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaleWriteBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String
    private lateinit var travelListId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaleWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
        userId = getSessionId()
        travelListId = intent.getStringExtra("travelListId") ?: ""

        databaseReference = FirebaseDatabase.getInstance().reference.child("TravelList").child(userId).child(travelListId)

        // 데이터 가져오기
        fetchTravelListData()

        // 저장 버튼 클릭 이벤트 처리
        binding.saveTaleButton.setOnClickListener {
            val taleText = binding.taleWrite.text.toString()
            if (taleText.isNotEmpty()) {
                saveTaleToDatabase(taleText)
            }
        }
    }

    private fun saveTaleToDatabase(taleText: String) {
        val newtalesid = generateUniqueId()
        val taleData = TaleData(talesid = newtalesid, text = taleText)
        databaseReference.child("tales").child(newtalesid).setValue(taleData)
            .addOnSuccessListener {
                // 저장 성공 시 처리
                val intent = Intent(this, TaleGetActivity::class.java)
                intent.putExtra("taleData", taleData)
                intent.putExtra("travelListId", travelListId)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // 저장 실패 시 처리
                Toast.makeText(this, "테일 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun fetchTravelListData() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val title = snapshot.child("title").value.toString()
                val date = snapshot.child("date").value.toString()
                val address = snapshot.child("address").value.toString()
                val travelImage = snapshot.child("travelImage").value.toString()

                // 가져온 데이터를 바인딩에 설정
                binding.taleWriteTitle.setText(title)
                binding.dateTxt.setText(date)
                binding.mapTxt.setText(address)

                Picasso.get().load(travelImage).into(binding.mainImg)

            }

            override fun onCancelled(error: DatabaseError) {
                // 에러 처리
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