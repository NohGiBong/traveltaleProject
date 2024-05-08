package com.example.traveltaleproject

import com.example.traveltaleproject.schedule.ScheduleActivity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.checklist.CheckListActivity
import com.example.traveltaleproject.databinding.ActivityGetBinding
import com.example.traveltaleproject.models.TaleData
import com.example.traveltaleproject.tale.TaleGetActivity
import com.example.traveltaleproject.tale.TaleWriteActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GetActivity : AppCompatActivity() {
    private lateinit var bottomNavigationHelper: BottomNavigationHelper
    private lateinit var binding: ActivityGetBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityGetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val startDateTxt = findViewById<TextView>(R.id.start_date_txt)
        val endDateTxt = findViewById<TextView>(R.id.start_date_txt)

        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val picker = builder.build()

        picker.addOnPositiveButtonClickListener {
            val startDateInMillis = it.first ?: return@addOnPositiveButtonClickListener
            val endDateInMillis = it.second ?: return@addOnPositiveButtonClickListener

            val sdf = SimpleDateFormat("dd. MMM. yyyy", Locale.ENGLISH)

            val startDate = Calendar.getInstance().apply { timeInMillis = startDateInMillis }
            val endDate = Calendar.getInstance().apply { timeInMillis = endDateInMillis }

            val formattedStartDate = sdf.format(startDate.time)
            val formattedEndDate = sdf.format(endDate.time)

            startDateTxt.text = formattedStartDate
            endDateTxt.text = formattedEndDate
        }

        val datePickerButton = findViewById<ImageButton>(R.id.date_btn)
        datePickerButton.setOnClickListener {
            picker.show(supportFragmentManager, picker.toString())
        }

        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
        userId = getSessionId()

        // Intent로 전달된 TravelListId 가져오기
        val travelListId = intent.getStringExtra("travelListId") ?: ""

        // Firebase Database의 Reference 설정
        databaseReference =
            FirebaseDatabase.getInstance().reference.child("TravelList").child(userId)
                .child(travelListId)

        // 데이터 가져오기
        fetchTravelListData()

        // 체크리스트 버튼 클릭 시 이벤트
        binding.getChecklistBtn.setOnClickListener {
            val intent = Intent(this, CheckListActivity::class.java)
            intent.putExtra("travelListId", travelListId)
            startActivity(intent)
        }

        // 여행 일정 버튼 클릭 시 이벤트
        binding.getScheduleBtn.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            intent.putExtra("travelListId", travelListId)
            startActivity(intent)
        }

        // TravelTale 작성하기 버튼 클릭 시 이벤트
        binding.getTaleBtn.setOnClickListener {
            val travelListId = intent.getStringExtra("travelListId") ?: ""
            val userId = getSessionId()

            // Firebase Database의 Reference 설정
            val talesRef = FirebaseDatabase.getInstance().getReference("TravelList").child(userId)
                .child(travelListId)

            talesRef.child("tales").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // 이야기가 존재하는 경우
                        val taleSnapshot = dataSnapshot.children.first() // 첫 번째 이야기 데이터를 가져옴
                        val taleId = taleSnapshot.key
                        val text = taleSnapshot.child("text").getValue(String::class.java)

                        if (taleId != null && text != null) {
                            // 이야기 데이터를 객체로 생성
                            val taleData = TaleData(taleId, text)

                            // TaleGetActivity로 이동하면서 이야기 데이터를 전달
                            val intent = Intent(this@GetActivity, TaleGetActivity::class.java)
                            intent.putExtra("taleData", taleData)
                            intent.putExtra("travelListId", travelListId)
                            startActivity(intent)
                        }
                    } else {
                        // 이야기가 없는 경우
                        val intent = Intent(this@GetActivity, TaleWriteActivity::class.java)
                        intent.putExtra("travelListId", travelListId)
                        startActivity(intent)
                    }
                }


                override fun onCancelled(databaseError: DatabaseError) {
                    // 에러 처리
                }
            })
        }

        // BottomNavigationHelper 초기화
        bottomNavigationHelper = BottomNavigationHelper(this, this)

        // 네비게이션 뷰의 아이템 선택 리스너 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationHelper.setupBottomNavigationListener(bottomNavigationView)

        // 여행 일정 제목 변경
        setUpTitle()

        // 여행 일정 날짜 변경

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
                binding.getTitle.setText(title)

                val sdf = SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH)
                val formattedStartDate = sdf.format(Date(startDate))
                val formattedEndDate = sdf.format(Date(endDate))

                binding.startDateTxt.text = formattedStartDate
                binding.endDateTxt.text = formattedEndDate

                binding.mapTxt.setText(address)

                Picasso.get().load(travelImage).into(binding.mainImg)

            }

            override fun onCancelled(error: DatabaseError) {
                // 에러 처리
            }
        })
    }

    // 제목 수정
    private fun setUpTitle() {
        val initialTitle = binding.getTitle.text.toString()

        binding.getTitle.setOnEditorActionListener { _, _, _ ->
            val newTitle = binding.getTitle.text.toString()

            if (newTitle != initialTitle) {
                showConfirmDialog(newTitle)
            }
            true
        }
    }

    private fun showConfirmDialog(newTitle: String) {
        val builder = AlertDialog.Builder(this, R.style.RoundedCornersDialog)
        builder.setTitle("제목을 변경하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                databaseReference.child("title").setValue(newTitle)
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(R.color.black))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.black))
        }

        dialog.show()
    }

    private fun getSessionId(): String {
        return sharedPreferences.getString("user_id", "").toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
