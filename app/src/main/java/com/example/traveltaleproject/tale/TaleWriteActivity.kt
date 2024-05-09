package com.example.traveltaleproject.tale

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.GetActivity
import com.example.traveltaleproject.checklist.CheckListActivity
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

        sharedPreferences = getSharedPreferences("MyInfo", MODE_PRIVATE)
        userId = getSessionId()
        travelListId = intent.getStringExtra("travelListId") ?: ""
        talesid = intent.getStringExtra("talesid")
        databaseReference =
            FirebaseDatabase.getInstance().reference.child("TravelList").child(userId)
                .child(travelListId)

        fetchTravelListData()

        binding.saveTaleButton.setOnClickListener {
            val taleText = binding.taleWrite.text.toString()
            if (taleText.isNotEmpty()) {
                saveTaleToDatabase(taleText)
            }
        }

        binding.backBtn.setOnClickListener {
            val intent = Intent(this, GetActivity::class.java)
            intent.putExtra("travelListId", travelListId)
            startActivity(intent)
        }
    }

    private fun fetchTravelListData() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setTravelListData(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // Tales DB 저장
    private fun saveTaleToDatabase(taleText: String) {
        val newtalesid = generateUniqueId()
        val taleData = TaleData(talesid = newtalesid, text = taleText)
        databaseReference.child("tales").child(newtalesid).setValue(taleData)
            .addOnSuccessListener { navigateToTaleGetActivity(taleData) }
            .addOnFailureListener { }
    }

    // TravelList Data 바인딩
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

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH)
        return sdf.format(Date(timestamp))
    }

    private fun getSessionId(): String {
        return sharedPreferences.getString("user_id", "").toString()
    }

    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    private fun navigateToTaleGetActivity(taleData: TaleData) {
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