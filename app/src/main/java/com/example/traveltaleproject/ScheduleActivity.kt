package com.example.traveltaleproject


import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityScheduleBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private lateinit var startDate: Calendar
    private lateinit var endDate: Calendar
    private var differenceInDays: Long? = null
    private lateinit var scheduleList: MutableList<String>
    private lateinit var adapter: ScheduleDayItemAdapter
    private lateinit var bottomNavigationHelper: BottomNavigationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dateEditText = findViewById<EditText>(R.id.date_txt)

        // Adapter 초기화
        scheduleList = mutableListOf()
        adapter = ScheduleDayItemAdapter(scheduleList)
        binding.scheduleDayItem.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) // 수평으로 설정
        binding.scheduleDayItem.adapter = adapter

        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val picker = builder.build()

        picker.addOnPositiveButtonClickListener {
            val startDateInMillis = it.first ?: return@addOnPositiveButtonClickListener
            val endDateInMillis = it.second ?: return@addOnPositiveButtonClickListener

            val sdf = SimpleDateFormat("dd. MMM. yyyy", Locale.ENGLISH)

            startDate = Calendar.getInstance().apply { timeInMillis = startDateInMillis }
            endDate = Calendar.getInstance().apply { timeInMillis = endDateInMillis }

            val formattedStartDate = sdf.format(startDate.time)
            val formattedEndDate = sdf.format(endDate.time)

            dateEditText.setText("$formattedStartDate - $formattedEndDate")

            // 두 날짜 간의 차이를 밀리초로 계산
            val differenceInMillis = endDate.timeInMillis - startDate.timeInMillis

            // 밀리초를 일로 변환
            differenceInDays = (differenceInMillis / (1000 * 60 * 60 * 24)) + 1

            // 토스트로 기간 출력
            val toast = Toast.makeText(this, differenceInDays.toString(), Toast.LENGTH_SHORT)
            toast.show()

            // differenceInDays가 null이 아닌 경우에만 실행
            differenceInDays?.let { days ->
                // Adapter에 데이터 추가 및 갱신
                scheduleList.clear()
                for (i in 1..days) {
                    scheduleList.add("$i 일차")
                }
                adapter.notifyDataSetChanged()
            }
        }

        val datePickerButton = findViewById<ImageButton>(R.id.date_btn)
        datePickerButton.setOnClickListener {
            picker.show(supportFragmentManager, picker.toString())
        }

        // BottomNavigationHelper 초기화
        bottomNavigationHelper = BottomNavigationHelper(this, this)

        // 네비게이션 뷰의 아이템 선택 리스너 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationHelper.setupBottomNavigationListener(bottomNavigationView)
    }
}