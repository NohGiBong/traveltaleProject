package com.example.traveltaleproject

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.traveltaleproject.checklist.CheckListActivity
import com.example.traveltaleproject.databinding.ActivityGetBinding
import com.example.traveltaleproject.models.TaleData
import com.example.traveltaleproject.schedule.ScheduleActivity
import com.example.traveltaleproject.tale.TaleGetActivity
import com.example.traveltaleproject.tale.TaleWriteActivity
import com.google.android.gms.tasks.Tasks
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
    private lateinit var travelListId: String
    private lateinit var scheduleDayList: MutableList<String>
    private var startDateIntent: Long? = null
    private var endDateIntent: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val startDateTxt = findViewById<TextView>(R.id.start_date_txt)
        val endDateTxt = findViewById<TextView>(R.id.end_date_txt)

        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { it ->
            val startDateInMillis = it.first ?: return@addOnPositiveButtonClickListener
            startDateIntent = startDateInMillis
            val endDateInMillis = it.second ?: return@addOnPositiveButtonClickListener
            endDateIntent = endDateInMillis

            val sdf = SimpleDateFormat("dd. MMM. yyyy", Locale.ENGLISH)

            val startDate = Calendar.getInstance().apply { timeInMillis = startDateInMillis }
            val endDate = Calendar.getInstance().apply { timeInMillis = endDateInMillis }

            val formattedStartDate = startDate.let { sdf.format(it.time) }
            val formattedEndDate = endDate.let { sdf.format(it.time) }

            startDateTxt.text = formattedStartDate
            endDateTxt.text = formattedEndDate

            // 변경된 날짜를 데이터베이스에 저장
            addNewFragmentsToDatabase()
        }

        val datePickerButton = findViewById<ImageButton>(R.id.date_btn)
        datePickerButton.setOnClickListener {
            picker.show(supportFragmentManager, picker.toString())
        }

        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
        userId = getSessionId()

        // Intent로 전달된 TravelListId 가져오기
        travelListId = intent.getStringExtra("travelListId") ?: ""

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
    }

    override fun onResume() {
        super.onResume()
        fetchTravelListData()
    }

    private fun fetchTravelListData() {
        // Firebase Database의 Reference 설정
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.child("title").value.toString()
                val startDateLong = snapshot.child("startDate").value as? Long
                val endDateLong = snapshot.child("endDate").value as? Long
                val address = snapshot.child("address").value.toString()
                val travelImage = snapshot.child("travelImage").value.toString()

                // startDateLong과 endDateLong이 null인 경우에 대한 처리
                if (startDateLong == null || endDateLong == null) {
                    // 처리할 로직 추가
                    return
                }

                // startDateLong과 endDateLong이 null이 아닌 경우에 대한 로직 추가

                // 가져온 데이터를 바인딩에 설정
                binding.getTitle.text.toString()

                val sdf = SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH)
                val formattedStartDate = sdf.format(startDateLong)
                val formattedEndDate = sdf.format(endDateLong)

                binding.startDateTxt.text = formattedStartDate
                binding.endDateTxt.text = formattedEndDate

                binding.mapTxt.text = address

                Picasso.get().load(travelImage).into(binding.mainImg)

                // scheduleDayList 초기화
                scheduleDayList = mutableListOf()
                for (data in snapshot.child("schedule").children) {
                    val day = data.child("daysection").getValue(String::class.java)
                    day?.let {
                        scheduleDayList.add(it)
                    }
                }

                // 데이터를 가져온 후에 addNewFragmentsToDatabase를 호출
                addNewFragmentsToDatabase()
            }

            override fun onCancelled(error: DatabaseError) {
                // 에러 처리
                showToast("Failed to fetch data: ${error.message}")
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
                .setTextColor(ContextCompat.getColor(this, R.color.black))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        dialog.show()
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
                for ((index, _) in scheduleDayList.withIndex()) {
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
