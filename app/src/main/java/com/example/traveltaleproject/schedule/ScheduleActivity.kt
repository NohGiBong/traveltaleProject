package com.example.traveltaleproject.schedule

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.BottomNavigationHelper
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityScheduleBinding
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ScheduleActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bottomNavigationHelper: BottomNavigationHelper
    private lateinit var binding: ActivityScheduleBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String
    private lateinit var travelListId: String

    private lateinit var startDate: Calendar
    private lateinit var endDate: Calendar
    private lateinit var startDateTxt: TextView
    private lateinit var endDateTxt: TextView
    private lateinit var scheduleDayList: MutableList<String>
    private lateinit var adapter: ScheduleDayToDayAdapter
    private lateinit var fragmentView: ViewGroup
    private var selectedDate: String? = null
    private var isDataSavedToDatabase = true

    private var startDateIntent: Long? = null
    private var endDateIntent: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
        userId = getSessionId()

        // Intent로 전달된 TravelListId 가져오기
        travelListId = intent.getStringExtra("travelListId") ?: ""

        // Firebase Database의 Reference 설정
        databaseReference = FirebaseDatabase.getInstance().reference.child("TravelList").child(userId).child(travelListId)

        // 날짜 가져오기
        fetchDateFromDB()

        val recyclerView = findViewById<RecyclerView>(R.id.schedule_day_item)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        fragmentView = findViewById(R.id.fragment_view)
        scheduleDayList = mutableListOf()
        adapter = ScheduleDayToDayAdapter(scheduleDayList)
        adapter.setOnItemClickListener(object : ScheduleDayToDayAdapter.OnItemClickListener {
            override fun onItemClick(date: String) {
                // 클릭한 아이템의 날짜를 전달하여 프래그먼트 생성
                val daySection = date.split("일차")[0] // "1일차" -> "1"
                showFragmentForDate(date, "day$daySection")
            }
        })
        recyclerView.adapter = adapter

        startDateTxt = findViewById(R.id.start_date_txt)
        endDateTxt = findViewById(R.id.end_date_txt)

        val dateBtn = findViewById<ImageButton>(R.id.date_btn)
        dateBtn.setOnClickListener {
            val builder = MaterialDatePicker.Builder.dateRangePicker()

            // 현재 선택된 시작 날짜와 종료 날짜를 기본으로 설정
            builder.setSelection(
                androidx.core.util.Pair(
                    startDate.timeInMillis,
                    endDate.timeInMillis
                )
            )

            val picker = builder.build()
            picker.show(supportFragmentManager, picker.toString())

            picker.addOnPositiveButtonClickListener { selection ->
                val startDateMillis = selection.first ?: return@addOnPositiveButtonClickListener
                startDateIntent = startDateMillis
                val endDateMillis = selection.second ?: return@addOnPositiveButtonClickListener
                endDateIntent = endDateMillis

                val startCalendar = Calendar.getInstance().apply { timeInMillis = startDateMillis }
                val endCalendar = Calendar.getInstance().apply { timeInMillis = endDateMillis }

                // applyDateRangePicker 메서드 호출
                applyDateRangePicker(startCalendar, endCalendar)

                // 데이터가 변경되었음을 표시
                isDataSavedToDatabase = true

                // 모든 프래그먼트를 invisible로 설정하여 숨김
                supportFragmentManager.fragments.forEach { fragment ->
                    fragment.view?.visibility = View.INVISIBLE
                }

                // RecyclerView 어댑터의 선택된 스타일을 디폴트로 설정
                adapter.setSelectedItem(null)

                // 변경된 스케줄을 데이터베이스에 저장
                addNewFragmentsToDatabase()
            }
        }

        // BottomNavigationHelper 초기화
        bottomNavigationHelper = BottomNavigationHelper(this, this)

        // 네비게이션 뷰의 아이템 선택 리스너 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationHelper.setupBottomNavigationListener(bottomNavigationView)
    }

    private fun fetchDateFromDB() {
        val databaseReference = FirebaseDatabase.getInstance().reference
            .child("TravelList").child(userId).child(travelListId)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // DB에 travelListId에 schedule 경로가 있으면 프래그먼트 식별자 생성 X
                    isDataSavedToDatabase = snapshot.hasChild("schedule")

                    // startDate와 endDate를 Calendar 객체로 변환
                    val startDateLong = snapshot.child("startDate").value as? Long
                    val endDateLong = snapshot.child("endDate").value as? Long

                    val startCalendar = Calendar.getInstance().apply { timeInMillis = startDateLong ?: 0 }
                    val endCalendar = Calendar.getInstance().apply { timeInMillis = endDateLong ?: 0 }

                    // DateRangePicker에 startDate와 endDate 적용
                    applyDateRangePicker(startCalendar, endCalendar)
                } else {
                    isDataSavedToDatabase = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Error handling
            }
        })
    }

    private fun applyDateRangePicker(startCalendar: Calendar, endCalendar: Calendar) {
        val sdf = SimpleDateFormat("dd. MMM. yyyy", Locale.ENGLISH)

        startDate = startCalendar
        endDate = endCalendar

        val formattedStartDate = sdf.format(startDate.time)
        val formattedEndDate = sdf.format(endDate.time)

        startDateTxt.text = formattedStartDate
        endDateTxt.text = formattedEndDate

        // 두 날짜 간의 차이를 밀리초로 계산
        val differenceInMillis = endDate.timeInMillis - startDate.timeInMillis

        // 밀리초를 일로 변환
        val differenceInDays: Long = (differenceInMillis / (1000 * 60 * 60 * 24)) + 1

        // Adapter에 데이터 추가 및 갱신
        scheduleDayList.clear()
        for (i in 1..differenceInDays) {
            scheduleDayList.add("$i" + "일차")
        }
        adapter.notifyDataSetChanged()

        // 리사이클러뷰를 표시하는 함수 호출
        showSchedulePeriod()

        // 선택된 날짜에 따라 자동으로 프래그먼트 표시
        if (!isDataSavedToDatabase) {
            addNewFragmentsToDatabase()
        }

        // 아이템 클릭 이벤트를 액티비티로 전달
        selectedDate?.let { date ->
            val daySection = "day${date.toInt()?.plus(1) ?: return@let}"
            showFragmentForDate(date, daySection)
        }
    }


    // 캘린더 선택 이벤트 발생 시 호출되는 함수
    private fun showSchedulePeriod() {
        binding.schedulePeriod.visibility = View.VISIBLE
    }

    // showFragmentForDate 함수 추가
    private fun showFragmentForDate(date: String?, daySection: String?) {
        if (daySection == null || date == null) {
            return
        }
        supportFragmentManager.beginTransaction().apply {
            // 이미 해당 태그를 가진 프래그먼트가 있는지 확인
            val existingFragment = supportFragmentManager.findFragmentByTag(date)
            if (existingFragment != null) {
                // 이미 추가된 경우 아무 작업도 하지 않고 종료
                return
            }

            // ScheduleFragment.newInstance 메서드로 새 프래그먼트를 생성하고 추가합니다.
            val newFragment = ScheduleFragment.newInstance(date.toString(), travelListId, userId)

            val bundle = Bundle()
            bundle.putString("daySection", daySection)
            newFragment.arguments = bundle

            replace(R.id.fragment_view, newFragment, daySection)
            commit()

            // 클릭한 날짜에 해당하는 프래그먼트를 보이도록 설정
            val fragmentLayout =
                (supportFragmentManager.findFragmentById(R.id.fragment_view)?.view?.parent as? ViewGroup)
            fragmentLayout?.visibility = View.VISIBLE
        }
    }

    private fun addNewFragmentsToDatabase() {
        // startDate 및 endDate가 null인지 확인
        if (startDateIntent == null || endDateIntent == null) {
            showToast("Error: Start date or end date is null.")
            return
        }

        // 최상위 노드 참조
        val databaseReference = FirebaseDatabase.getInstance().reference
            .child("TravelList")
            .child(userId)
            .child(travelListId)

        // 이전 스케줄 데이터 삭제를 위한 쿼리
        val removeScheduleQuery = databaseReference.child("schedule").removeValue()
        val removeStartDateQuery = databaseReference.child("startDate").removeValue()
        val removeEndDateQuery = databaseReference.child("endDate").removeValue()
        val removeDateQuery = databaseReference.child("date").removeValue()

        // 스케줄, 시작일, 종료일, 날짜 삭제가 모두 성공적으로 이루어지면 새로운 데이터 추가를 수행
        val tasks = listOf(removeScheduleQuery, removeStartDateQuery, removeEndDateQuery, removeDateQuery)
        Tasks.whenAllSuccess<Void>(tasks)
            .addOnSuccessListener {
                val startDateIntent = startDateIntent ?: 0
                val endDateIntent = endDateIntent ?: 0

                databaseReference.child("startDate").setValue(startDateIntent)
                databaseReference.child("endDate").setValue(endDateIntent)

                val formattedStartDate = SimpleDateFormat("dd. MMM. yyyy", Locale.ENGLISH).format(Date(startDateIntent))
                val formattedEndDate = SimpleDateFormat("dd. MMM. yyyy", Locale.ENGLISH).format(Date(endDateIntent))
                databaseReference.child("date").setValue("$formattedStartDate - $formattedEndDate")

                // 성공적으로 이전 스케줄 데이터가 삭제된 경우에만 새로운 스케줄 데이터 추가
                for ((index, day) in scheduleDayList.withIndex()) {
                    val daySection = "day${index + 1}"

                    // 해당 날짜에 대한 데이터 생성
                    val fragmentData = HashMap<String, Any>()
                    fragmentData["daysection"] = daySection

                    // 데이터베이스에 새로운 스케줄 추가
                    databaseReference.child("schedule").child(daySection).setValue(fragmentData)
                        .addOnSuccessListener {
                            // 성공적으로 추가된 경우 아무것도 하지 않음
                        }
                        .addOnFailureListener {
                            // 실패한 경우 에러 처리
                            showToast("Failed to add new schedule data.")
                        }
                }
            }
            .addOnFailureListener { exception ->
                // 하나 이상의 삭제 작업이 실패한 경우
                showToast("Failed to remove previous data: ${exception.message}")
            }
    }

    private fun getSessionId(): String {
        return sharedPreferences.getString("user_id", "").toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
