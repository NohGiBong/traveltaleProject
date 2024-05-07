package com.example.traveltaleproject.schedule

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.traveltaleproject.BottomNavigationHelper
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityScheduleBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleActivity : AppCompatActivity() {

    private lateinit var startDate: Calendar
    private lateinit var endDate: Calendar
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bottomNavigationHelper: BottomNavigationHelper
    private lateinit var binding: ActivityScheduleBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String

    private lateinit var startDateTxt: TextView
    private lateinit var endDateTxt: TextView
    private lateinit var scheduleDayList: MutableList<String>
    private lateinit var adapter: ScheduleDayToDayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
        userId = getSessionId()

        // Intent로 전달된 TravelListId 가져오기
        val travelListId = intent.getStringExtra("travelListId") ?: ""

        // Firebase Database의 Reference 설정
        databaseReference = FirebaseDatabase.getInstance().reference.child("TravelList").child(userId).child(travelListId)

        // 날짜 가져오기
        fetchDateFromDB()

        val recyclerView = binding.scheduleDayItem
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        scheduleDayList = mutableListOf<String>()
        adapter = ScheduleDayToDayAdapter(scheduleDayList)
        adapter.setFragmentManager(supportFragmentManager) // Activity에서 사용하는 경우
        recyclerView.adapter = adapter

        startDateTxt = binding.startDateTxt
        endDateTxt = binding.endDateTxt

        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val picker = builder.build()

        binding.dateBtn.setOnClickListener {
            picker.show(supportFragmentManager, picker.toString())
        }

        // 리사이클러뷰 아이템 클릭 리스너 설정
        recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(this, recyclerView, object : RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    val clickedItem = scheduleDayList[position]
                    // 아이템 클릭 이벤트를 처리하여 프래그먼트 표시
                    showFragmentForDate(clickedItem)
                }

                override fun onLongItemClick(view: View?, position: Int) {
                    // Do nothing
                }
            })
        )

        // BottomNavigationHelper 초기화
        bottomNavigationHelper = BottomNavigationHelper(this, this)

        // 네비게이션 뷰의 아이템 선택 리스너 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationHelper.setupBottomNavigationListener(bottomNavigationView)
    }

    private fun fetchDateFromDB() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val startDate = snapshot.child("startDate").value as Long
                    val endDate = snapshot.child("endDate").value as Long

                    // startDate와 endDate를 Calendar 객체로 변환
                    val startCalendar = Calendar.getInstance().apply { timeInMillis = startDate }
                    val endCalendar = Calendar.getInstance().apply { timeInMillis = endDate }

                    // DateRangePicker에 startDate와 endDate 적용
                    applyDateRangePicker(startCalendar, endCalendar)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun applyDateRangePicker(startCalendar: Calendar, endCalendar: Calendar) {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setSelection(
                androidx.core.util.Pair(
                    startCalendar.timeInMillis,
                    endCalendar.timeInMillis
                )
            )
            .build()

        picker.addOnPositiveButtonClickListener {
            val startDateInMillis = it.first ?: return@addOnPositiveButtonClickListener
            val endDateInMillis = it.second ?: return@addOnPositiveButtonClickListener

            val sdf = SimpleDateFormat("dd. MMM. yyyy", Locale.ENGLISH)

            startDate = Calendar.getInstance().apply { timeInMillis = startDateInMillis }
            endDate = Calendar.getInstance().apply { timeInMillis = endDateInMillis }

            val formattedStartDate = sdf.format(startDate.time)
            val formattedEndDate = sdf.format(endDate.time)

            startDateTxt.text = formattedStartDate
            endDateTxt.text = formattedEndDate

            // 두 날짜 간의 차이를 밀리초로 계산
            val differenceInMillis = endDate.timeInMillis - startDate.timeInMillis

            // 밀리초를 일로 변환
            val differenceInDays: Long = (differenceInMillis / (1000 * 60 * 60 * 24)) + 1

            // 토스트로 기간 출력
            showToast("기간 출력 : $differenceInDays.toString()")

            // Adapter에 데이터 추가 및 갱신
            scheduleDayList.clear()
            for (i in 1..differenceInDays) {
                scheduleDayList.add("$i" + "일차")
            }
            adapter.notifyDataSetChanged()

            // 리사이클러뷰를 표시하는 함수 호출
            showSchedulePeriod()
        }

        picker.show(supportFragmentManager, "DateRangePicker")
    }

    // 캘린더 선택 이벤트 발생 시 호출되는 함수
    private fun showSchedulePeriod() {
        binding.schedulePeriod.visibility = View.VISIBLE
    }

    // showFragmentForDate 함수 추가
    private fun showFragmentForDate(date: String) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        val fragment = ScheduleFragment.newInstance(date)
        transaction.replace(binding.fragmentView.id, fragment)
        transaction.commit()
    }

    private fun getSessionId(): String {
        return sharedPreferences.getString("user_id", "").toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
