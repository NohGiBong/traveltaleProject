package com.example.traveltaleproject

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.databinding.ActivityChecklistBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CheckListActivity : AppCompatActivity() {
    private lateinit var bottomNavigationHelper: BottomNavigationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dateEditText = findViewById<TextView>(R.id.date_text)

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

            dateEditText.setText("$formattedStartDate - $formattedEndDate")
        }

        val datePickerButton = findViewById<ImageButton>(R.id.date_btn)
        datePickerButton.setOnClickListener {
            picker.show(supportFragmentManager, picker.toString())
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val datalist = mutableListOf<String>()
        val adapter = ChecklistAdapter(datalist)
        recyclerView.adapter = adapter

        // submitBtn 클릭 시 checklist_item 출력
        binding.submitBtn.setOnClickListener {
            val newItem = ""

            datalist.add(newItem)

            adapter.notifyItemInserted(datalist.size -1)
        }

        // BottomNavigationHelper 초기화
        bottomNavigationHelper = BottomNavigationHelper(this, this)

        // 네비게이션 뷰의 아이템 선택 리스너 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationHelper.setupBottomNavigationListener(bottomNavigationView)
    }
}
