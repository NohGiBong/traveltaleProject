package com.example.traveltaleproject.tale

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.GetActivity
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityTaleGetBinding
import com.example.traveltaleproject.databinding.ActivityTaleWriteBinding
import com.example.traveltaleproject.models.TaleData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class TaleGetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaleGetBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaleGetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val travelListId = intent.getStringExtra("travelListId") ?: ""
        val taleData = intent.getParcelableExtra<TaleData>("taleData")

        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
        userId = getSessionId()

        binding.taleGet.text = taleData?.text?: ""

        databaseReference = FirebaseDatabase.getInstance().reference.child("TravelList").child(userId).child(travelListId)

        // 데이터 가져오기
        fetchTravelListData()

        // 삭제 후 write로 돌아가는 메서드
        fun goToWriteActivity() {
            val intent = Intent(this, GetActivity::class.java)
            intent.putExtra("travelListId", travelListId)
            startActivity(intent)
            finish()
        }

        with(binding) {
            menuBtn.setOnClickListener {
                val popupMenu = PopupMenu(this@TaleGetActivity, it)
                menuInflater.inflate(R.menu.tale_menu, popupMenu.menu)
                // 콜백 메서드 구현
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            val intent = Intent(this@TaleGetActivity, TaleEditActivity::class.java)
                            if (taleData != null) {
                                // 수정할 데이터의 고유 ID 전달
                                intent.putExtra("talesid", taleData.talesid)
                            }
                            startActivity(intent)
                            finish()

                            Toast.makeText(this@TaleGetActivity, "수정 클릭", Toast.LENGTH_SHORT).show()
                        }
                        R.id.action_delete -> {
                            // 데이터베이스에 저장된 talesid를 기준으로 현재 불러온 게시물 삭제
                            databaseReference.child("tales").child(taleData?.talesid.toString()).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(this@TaleGetActivity, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                    goToWriteActivity() // 삭제 후 write로 돌아가는 메서드 호출
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this@TaleGetActivity, "게시물 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    false
                }
                popupMenu.show()
            }
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
                binding.taleGetTitle.setText(title)
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
}