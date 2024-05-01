package com.example.traveltaleproject

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityScheduleBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    lateinit var startDate: Calendar
    lateinit var endDate: Calendar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dateEditText = findViewById<EditText>(R.id.date_txt)

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
            val differenceInDays = (differenceInMillis / (1000 * 60 * 60 * 24)) + 1

            // 토스트로 기간 출력
            val toast = Toast.makeText(this, differenceInDays.toString(), Toast.LENGTH_SHORT)
            toast.show()



        }

        val datePickerButton = findViewById<ImageButton>(R.id.date_btn)
        datePickerButton.setOnClickListener {
            picker.show(supportFragmentManager, picker.toString())
        }

    }



}