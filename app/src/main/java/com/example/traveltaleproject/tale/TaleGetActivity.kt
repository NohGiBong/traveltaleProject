package com.example.traveltaleproject.tale

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var bottomNavigationHelper: BottomNavigationHelper

    private var initialMarginTop = 180 // 초기 마진 값
    private val maxMarginTop = 0 // 최소 마진 값
    private lateinit var scrollView: ScrollView
    private lateinit var taleGetBodyLayout: RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaleGetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 데이터 초기화
        val travelListId = intent.getStringExtra("travelListId") ?: ""
        val taleData = intent.getParcelableExtra<TaleData>("taleData")
        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
        userId = getSessionId()
        binding.taleGet.text = taleData?.text?: ""
        databaseReference = FirebaseDatabase.getInstance().reference.child("TravelList").child(userId).child(travelListId)

        // 데이터 가져오기
        fetchTravelListData()

        // scrollView 초기화
        scrollView = findViewById(R.id.scroll_view)
        taleGetBodyLayout = findViewById(R.id.tale_get_body)

        // ScrollView의 스크롤 이벤트 감지
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY // 현재 스크롤 위치

            // 초기 마진에서 현재 스크롤 위치를 빼서 새로운 마진 값을 계산
            val newMarginTop = initialMarginTop - scrollY

            // 최소 마진 값을 벗어나지 않도록 조정
            val adjustedMarginTop = maxOf(newMarginTop, maxMarginTop)

            // RelativeLayout의 LayoutParams를 가져와서 마진을 조정
            val params = taleGetBodyLayout.layoutParams as FrameLayout.LayoutParams
            params.topMargin = adjustedMarginTop
            taleGetBodyLayout.layoutParams = params
        }

        fun goToWriteActivity() {
            val intent = Intent(this, GetActivity::class.java)
            intent.putExtra("travelListId", travelListId)
            startActivity(intent)
            finish()
        }

        // 메뉴 버튼 구현
        with(binding) {
            menuBtn.setOnClickListener {
                val popupMenu = PopupMenu(this@TaleGetActivity, it)
                menuInflater.inflate(R.menu.tale_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> {
                            val intent = Intent(this@TaleGetActivity, TaleEditActivity::class.java)

                            if (taleData != null) {
                                intent.putExtra("talesid", taleData.talesid)
                                intent.putExtra("travelListId", travelListId)
                            }
                            startActivity(intent)
                            finish()
                        }
                        R.id.action_delete -> {
                            databaseReference.child("tales").child(taleData?.talesid.toString()).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(this@TaleGetActivity, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                    goToWriteActivity()
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

        // BottomNavigationHelper 초기화
        bottomNavigationHelper = BottomNavigationHelper(this, this)

        // 네비게이션 뷰의 아이템 선택 리스너 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationHelper.setupBottomNavigationListener(bottomNavigationView)
    }

    private fun fetchTravelListData() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val title = snapshot.child("title").value.toString()
                val startDate = snapshot.child("startDate").value as Long
                val endDate = snapshot.child("endDate").value as Long
                val address = snapshot.child("address").value.toString()
                val travelImage = snapshot.child("travelImage").value.toString()

                // 가져온 데이터를 바인딩에 설정
                binding.taleGetTitle.setText(title)
                val sdf = SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH)
                val formattedStartDate = sdf.format(Date(startDate))
                val formattedEndDate = sdf.format(Date(endDate))

                binding.startDateTxt.text = formattedStartDate
                binding.endDateTxt.text = formattedEndDate
                binding.mapTxt.setText(address)
                Picasso.get().load(travelImage).into(binding.mainImg)
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }
        })
    }

    private fun getSessionId(): String {
        return sharedPreferences.getString("user_id", "").toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}