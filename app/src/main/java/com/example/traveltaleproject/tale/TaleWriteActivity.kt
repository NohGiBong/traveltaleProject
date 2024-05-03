package com.example.traveltaleproject.tale

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.databinding.ActivityTaleWriteBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TaleWriteActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTaleWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 저장 버튼 클릭 이벤트 처리
        binding.saveTaleButton.setOnClickListener {
            val taleText = binding.taleWrite.text.toString()
            if (taleText.isNotEmpty()) {
                saveTaleToDatabase(taleText)
            }
        }

        // Firebase Realtime Database 초기화
        database = FirebaseDatabase.getInstance().reference
    }

    private fun saveTaleToDatabase(taleText: String) {
        val newAid = generateUniqueId()
        val taleData = TaleData(aid = newAid, text = taleText)
        database.child("tales").child(newAid).setValue(taleData)
            .addOnSuccessListener {
                // 저장 성공 시 처리
                val intent = Intent(this, TaleGetActivity::class.java)
                intent.putExtra("taleData", taleData)
                startActivity(intent)
                finish()

                Toast.makeText(this, "테일 저장 성공", Toast.LENGTH_SHORT).show()
                Log.d("aaa", taleText)
            }
            .addOnFailureListener { e ->
                // 저장 실패 시 처리
                Toast.makeText(this, "테일 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateUniqueId(): String {
        // 고유 ID 생성 로직 구현
        // 예: 현재 시간을 기반으로 고유 ID 생성
        return System.currentTimeMillis().toString()
    }
}