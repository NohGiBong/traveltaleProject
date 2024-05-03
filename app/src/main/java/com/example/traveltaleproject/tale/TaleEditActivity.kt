package com.example.traveltaleproject.tale

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.databinding.ActivityTaleEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TaleEditActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var taleData: TaleData // 기존 데이터를 저장할 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityTaleEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Realtime Database 초기화
        database = FirebaseDatabase.getInstance().reference

        // 기존 데이터 불러오기
        val aid = intent.getStringExtra("aid") // 수정할 데이터의 고유 ID
        if (aid != null) {
            database.child("tales").child(aid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    taleData = dataSnapshot.getValue(TaleData::class.java)!!
                    binding.taleEdit.setText(taleData.text) // EditText에 기존 데이터 표시
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 데이터 불러오기 실패 시 처리
                    Toast.makeText(this@TaleEditActivity, "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 저장 버튼 클릭 이벤트 처리
        binding.editTaleButton.setOnClickListener {
            val taleText = binding.taleEdit.text.toString()
            if (taleText.isNotEmpty()) {
                updateTaleInDatabase(taleText)
            }
        }


    }

    private fun updateTaleInDatabase(taleText: String) {
        // 기존 데이터 업데이트
        taleData.text = taleText
        database.child("tales").child(taleData.aid).setValue(taleData)
            .addOnSuccessListener {
                // 업데이트 성공 시 처리
                val intent = Intent(this, TaleGetActivity::class.java)
                intent.putExtra("taleData", taleData)
                startActivity(intent)
                finish()

                Toast.makeText(this, "테일 수정 성공", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // 업데이트 실패 시 처리
                Toast.makeText(this, "테일 수정 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}