package com.example.traveltaleproject.schedule

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityScheduleModalBinding

class CustomModal(context: Context) : Dialog(context) {
    private lateinit var binding: ActivityScheduleModalBinding

    private var scheduleData: ScheduleData? = null

    fun setScheduleData(scheduleData: ScheduleData) {
        this.scheduleData = scheduleData
    }

    fun getScheduleData(): ScheduleData? {
        return scheduleData
    }

    data class ScheduleData(
        val startTime: Long,
        val endTime: Long,
        val scheduleText: String?
    )

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

        var selectedStartTime: Long = 0
        var selectedEndTime: Long = 0

        startTimeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedString = parent?.getItemAtPosition(position) as? String
                selectedStartTime = selectedString?.substring(0, 2)?.toLongOrNull() ?: 0
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        endTimeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedString = parent?.getItemAtPosition(position) as? String
                selectedEndTime = selectedString?.substring(0, 2)?.toLongOrNull() ?: 0
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        submitBtn.setOnClickListener {
            val scheduleText = scheduleTxtEdit.text.toString()

            if (scheduleText.isNotBlank() && selectedStartTime != selectedEndTime) {
                val scheduleData = ScheduleData(selectedStartTime, selectedEndTime, scheduleText)
                this.scheduleData = scheduleData
                scheduleDataListener?.onScheduleDataReceived(scheduleData)

                dismiss()
//                Toast.makeText(context, "Start Time: ${scheduleData.startTime}, End Time: ${scheduleData.endTime}, Schedule: ${scheduleData.scheduleText}", Toast.LENGTH_SHORT).show()

            } else if (scheduleText.isBlank()) {
                val modalScheduleTxtBox = binding.modalScheduleEditCard
                modalScheduleTxtBox.setCardBackgroundColor(context.getColor(R.color.error))
                Toast.makeText(context, "Please select start and end time and fill in the schedule", Toast.LENGTH_SHORT).show()
            } else if (selectedStartTime == selectedEndTime || selectedStartTime >= selectedEndTime) {
                startTimeSpinner.setBackgroundResource(R.drawable.error_spinner)
                endTimeSpinner.setBackgroundResource(R.drawable.error_spinner)
            }
        }
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

