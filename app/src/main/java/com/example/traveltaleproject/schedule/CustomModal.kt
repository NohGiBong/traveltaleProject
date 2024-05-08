package com.example.traveltaleproject.schedule

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.adapters.NumberPickerBindingAdapter.setValue
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityScheduleModalBinding
import com.example.traveltaleproject.models.ScheduleData
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class CustomModal(
    context: Context,
    private val daySection: String,
    private val travelListId: String,
    private val userId: String,
) : Dialog(context) {
    private lateinit var binding: ActivityScheduleModalBinding
    private var scheduleData: ScheduleData? = null
    private var selectedStartTime: Long = 0
    private var selectedEndTime: Long = 0
    private var scheduleText: String = ""

    fun setScheduleData(scheduleData: ScheduleData) {
        this.scheduleData = scheduleData
    }

    fun getScheduleData(): ScheduleData? {
        return scheduleData
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleModalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(true)
        setCancelable(true)

        val startTimeSpinner = binding.startTimeSpinner
        val endTimeSpinner = binding.endTimeSpinner
        val scheduleTxtEdit = binding.modalScheduleEdit
        val submitBtn = binding.scheduleAdd

        showToast("$userId", context)

        startTimeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedString = parent?.getItemAtPosition(position) as? String
                selectedStartTime = selectedString?.substring(0, 2)?.toLongOrNull() ?: 0
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        endTimeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedString = parent?.getItemAtPosition(position) as? String
                selectedEndTime = selectedString?.substring(0, 2)?.toLongOrNull() ?: 0
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        submitBtn.setOnClickListener {
            scheduleText = scheduleTxtEdit.text.toString()

            if (scheduleText.isNotBlank() && selectedStartTime != selectedEndTime) {
                val scheduleData = ScheduleData(selectedStartTime, selectedEndTime, scheduleText)
//                this.scheduleData = scheduleData
                saveScheduleDataToDatabase(scheduleData)
                scheduleDataListener?.onScheduleDataReceived(scheduleData)

                dismiss()

            } else if (scheduleText.isBlank()) {
                val modalScheduleTxtBox = binding.modalScheduleEditCard
                modalScheduleTxtBox.setCardBackgroundColor(context.getColor(R.color.error))
                Toast.makeText(
                    context,
                    "Please select start and end time and fill in the schedule",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (selectedStartTime == selectedEndTime || selectedStartTime >= selectedEndTime) {
                startTimeSpinner.setBackgroundResource(R.drawable.error_spinner)
                endTimeSpinner.setBackgroundResource(R.drawable.error_spinner)
            }
        }
    }

    private fun saveScheduleDataToDatabase(scheduleData: ScheduleData) {
        val userId = userId
        val travelListId = travelListId
        val scheduleTimeId = UUID.randomUUID().toString()
        val databaseReference = FirebaseDatabase.getInstance().reference.child("TravelList").child(userId).child(travelListId)

        // 해당 daysection 노드에 데이터 저장
        databaseReference.child("schedule").child(daySection).child(scheduleTimeId).setValue(scheduleData)
            .addOnSuccessListener {
                Log.d("dbReal", "성공")
            }
            .addOnFailureListener {
                Log.d("dbReal", "실패")
            }
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // 저장 버튼 클릭 리스너 인터페이스
    interface ScheduleDataListener {
        fun onScheduleDataReceived(scheduleData: ScheduleData)
    }

    private var scheduleDataListener: ScheduleDataListener? = null

    fun setScheduleDataListener(listener: ScheduleDataListener) {
        scheduleDataListener = listener
    }

}

