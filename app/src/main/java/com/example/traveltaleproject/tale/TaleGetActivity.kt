package com.example.traveltaleproject.tale

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.traveltaleproject.BottomNavigationHelper
import com.example.traveltaleproject.GetActivity
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityTaleGetBinding
import com.example.traveltaleproject.models.TaleData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaleGetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaleGetBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String
    private lateinit var travelListId: String
    private var taleData: TaleData? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaleGetBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 주요 기능 호출
        initializeData()
        fetchTravelListData()
        setupMenuButton()
        setupBottomNavigation()
    }

    // 데이터 초기화
    private fun initializeData() {
        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
        userId = getSessionId()
        travelListId = intent.getStringExtra("travelListId") ?: ""

        // Use the recommended getParcelableExtra method for older versions of Android
        @Suppress("DEPRECATION")
        taleData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("taleData", TaleData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("taleData")
        } ?: TaleData()

        binding.taleGet.text = taleData?.text ?: ""
        databaseReference = FirebaseDatabase.getInstance().reference.child("TravelList").child(userId).child(travelListId)
    }

    // 메뉴 버튼
    private fun setupMenuButton() {
        binding.menuBtn.setOnClickListener {
            val popupMenu = PopupMenu(this@TaleGetActivity, it)
            menuInflater.inflate(R.menu.tale_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        navigateToTaleWriteActivity()
                        true
                    }
                    R.id.action_delete -> {
                        showDeleteDialog()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    // 수정 버튼 클릭 시 기존 데이터 전달 후 글쓰기 페이지로 이동
    private fun navigateToTaleWriteActivity() {
        val intent = Intent(this, TaleWriteActivity::class.java)
        intent.putExtra("talesid", taleData?.talesid)
        intent.putExtra("travelListId", travelListId)
        startActivity(intent)
        finish()
    }

    // 삭제 버튼 클릭 시 경고창 호출
    private fun showDeleteDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("정말로 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ -> deleteTale() }
            .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.black))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        dialog.show()
    }

    // 경고창에서 삭제 선택 시
    private fun deleteTale() {
        taleData?.talesid?.let { talesId ->
            databaseReference.child("tales").child(talesId).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    navigateToGetActivity()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "게시물 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // 삭제 후 상위 페이지로 이동
    private fun navigateToGetActivity() {
        val intent = Intent(this, GetActivity::class.java)
        intent.putExtra("travelListId", travelListId)
        startActivity(intent)
        finish()
    }

    // // 상위 게시물 데이터 불러오기 및 UI 업데이트
    private fun fetchTravelListData() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val title = snapshot.child("title").value.toString()
                val startDate = snapshot.child("startDate").value as Long
                val endDate = snapshot.child("endDate").value as Long
                val address = snapshot.child("address").value.toString()
                val travelImage = snapshot.child("travelImage").value.toString()

                binding.taleGetTitle.setText(title)
                val sdf = SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH)
                binding.startDateTxt.text = sdf.format(Date(startDate))
                binding.endDateTxt.text = sdf.format(Date(endDate))
                binding.mapTxt.setText(address)
                Picasso.get().load(travelImage).into(binding.mainImg)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TaleGetActivity, "데이터 로드 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 하단 바 네비게이션
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        BottomNavigationHelper(this, this).setupBottomNavigationListener(bottomNavigationView)
    }

    // 사용자 정보 획득
    private fun getSessionId(): String {
        return sharedPreferences.getString("user_id", "") ?: ""
    }
}