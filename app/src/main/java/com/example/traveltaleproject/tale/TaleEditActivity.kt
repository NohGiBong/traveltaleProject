package com.example.traveltaleproject.tale

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.databinding.ActivityTaleEditBinding
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

class TaleEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaleEditBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String
    private lateinit var travelListId: String
    private var talesid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaleEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyInfo", MODE_PRIVATE)
        userId = getSessionId()
        travelListId = intent.getStringExtra("travelListId") ?: ""
        talesid = intent.getStringExtra("talesid")
        databaseReference = FirebaseDatabase.getInstance().reference.child("TravelList").child(userId).child(travelListId)

        // 뷰단에 기존 data 바인딩
        fetchTaleData()

        binding.saveTaleButton.setOnClickListener {
            val newText = binding.taleEdit.text.toString()
            if (newText.isNotEmpty()) {
                updateTale(newText)
            }
        }
    }

    private fun updateTale(newText: String) {
        if(talesid != null) {
            val taleTextRef = databaseReference.child("tales").child(talesid!!)

            val updatedText = hashMapOf<String, Any>(
                "text" to newText
            )

            taleTextRef.updateChildren(updatedText)
                .addOnSuccessListener {
                    fetchUpdatedTaleData()
                }
                .addOnFailureListener {
                    showToast("수정 실패")
                }
        } else {
            showToast("talesId가 존재하지않습니다.")
        }
    }

    // 수정된 data 가져오기
    private fun fetchUpdatedTaleData() {
        if (talesid != null) {
            val taleListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val taleData = snapshot.getValue(TaleData::class.java)
                    taleData?.let {
                        startTaleGetActivity(it) // 수정된 data 인텐트로 전달
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    //
                }
            }
            databaseReference.child("tales").child(talesid!!).addListenerForSingleValueEvent(taleListener)
        }
    }

    private fun fetchTaleData() {
        if (talesid != null) {
            // tales에 저장된 data 가져오기
            val taleListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val taleData = snapshot.getValue(TaleData::class.java)
                    taleData?.let { binding.taleEdit.setText(it.text) }
                }
                override fun onCancelled(error: DatabaseError) {
                    //
                }
            }

            // TravelList Data 불러오는 리스너
            val travelListListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    setTravelListData(snapshot)

                    databaseReference.child("tales").child(talesid!!).addListenerForSingleValueEvent(taleListener)
                }
                override fun onCancelled(error: DatabaseError) {
                    //
                }
            }

            databaseReference.addListenerForSingleValueEvent(travelListListener)
        }
    }


    // TravelList Data 바인딩
    private fun setTravelListData(snapshot: DataSnapshot) {
        val title = snapshot.child("title").value.toString()
        val startDate = snapshot.child("startDate").value as Long
        val endDate = snapshot.child("endDate").value as Long
        val address = snapshot.child("address").value.toString()
        val travelImage = snapshot.child("travelImage").value.toString()

        binding.taleEditTitle.setText(title)
        binding.startDateTxt.text = formatDate(startDate)
        binding.endDateTxt.text = formatDate(endDate)
        binding.mapTxt.text = address
        Picasso.get().load(travelImage).into(binding.mainImg)
    }
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH)
        return sdf.format(Date(timestamp))
    }
    private fun getSessionId(): String {
        return sharedPreferences.getString("user_id", "").toString()
    }

    private fun startTaleGetActivity(taleData: TaleData) {
        val intent = Intent(this, TaleGetActivity::class.java)
        intent.putExtra("taleData", taleData)
        intent.putExtra("travelListId", travelListId)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}