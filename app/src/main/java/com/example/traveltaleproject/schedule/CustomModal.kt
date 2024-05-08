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
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityScheduleModalBinding
import com.example.traveltaleproject.models.ScheduleData
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class CustomModal(
    context: Context,
    private val userId: String,
    private val travelListId: String,
    private val daySection: String
) : Dialog(context) {
    private lateinit var binding: ActivityScheduleModalBinding
    private var scheduleData: ScheduleData? = null
    private var selectedStartTime: Long = 0
    private var selectedEndTime: Long = 0
    private var scheduleText: String = ""
    var scheduleTimeId: String = ""

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

        // 수정 모드인지 새로운 추가 모드인지 구분하기 위한 플래그
        val isEditMode = (scheduleData != null)

        if (isEditMode) {
            // 수정 모드일 때, 전달받은 아이템의 정보를 사용하여 모달창에 내용 채우기
            startTimeSpinner.setSelection(scheduleData?.startTime?.toInt() ?: 0)
            endTimeSpinner.setSelection(scheduleData?.endTime?.toInt() ?: 0)
            scheduleTxtEdit.setText(scheduleData?.scheduleText)
        }

        submitBtn.setOnClickListener {
            scheduleText = scheduleTxtEdit.text.toString()

            if (scheduleText.isNotBlank() && selectedStartTime != selectedEndTime) {
                if (scheduleData != null && scheduleData?.scheduleTimeId == scheduleTimeId) {
                    // scheduleData가 null이 아니고 scheduleTimeId가 일치하면 수정 모드입니다.
                    // 기존의 일정을 업데이트합니다.
                    showToast("일정 업데이트 성공", context)
                    scheduleData?.startTime = selectedStartTime
                    scheduleData?.endTime = selectedEndTime
                    scheduleData?.scheduleText = scheduleText

                    val updatedScheduleData = ScheduleData(selectedStartTime, selectedEndTime, scheduleText, scheduleTimeId)
                    updateScheduleData(updatedScheduleData)
                } else {
                    createScheduleData(selectedStartTime, selectedEndTime, scheduleText)
                    saveScheduleDataToDatabase(scheduleData)
                    scheduleData?.let { it1 -> scheduleDataListener?.onScheduleDataReceived(it1) }
                }
                dismiss()
            } else if (scheduleText.isBlank()) {
                val modalScheduleTxtBox = binding.modalScheduleEditCard
                modalScheduleTxtBox.setCardBackgroundColor(context.getColor(R.color.error))
                Toast.makeText(
                    context,
                    "시작 시간과 종료 시간을 선택하고 일정을 입력해주세요",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (selectedStartTime == selectedEndTime || selectedStartTime >= selectedEndTime) {
                startTimeSpinner.setBackgroundResource(R.drawable.error_spinner)
                endTimeSpinner.setBackgroundResource(R.drawable.error_spinner)
            }
        }
    }

    private fun createScheduleData(startTime: Long, endTime: Long, scheduleText: String): ScheduleData {
        val scheduleTimeId = UUID.randomUUID().toString()
        this.scheduleTimeId = scheduleTimeId // 클래스 내부의 scheduleTimeId 업데이트
        return ScheduleData(startTime, endTime, scheduleText, scheduleTimeId)
    }

    private fun saveScheduleDataToDatabase(scheduleData: ScheduleData?) {
        if (scheduleData != null) {
            updateScheduleData(scheduleData)
        } else {
            val databaseReference = FirebaseDatabase.getInstance().reference
                .child("TravelList").child(userId).child(travelListId)

            // 해당 daysection 노드에 데이터 저장
            val scheduleRef = databaseReference.child("schedule").child(daySection)

            // scheduleTimeId를 사용하여 데이터 저장
            scheduleRef.child(scheduleTimeId).setValue(scheduleData)
                .addOnSuccessListener {
                    Log.d("dbReal", "추가 성공")
                }
                .addOnFailureListener {
                    Log.d("dbReal", "추가 실패")
                }
        }
    }

    private fun updateScheduleData(scheduleDataToUpdate: ScheduleData) {
        val databaseReference = FirebaseDatabase.getInstance().reference
            .child("TravelList").child(userId).child(travelListId)

        // 해당 daysection 노드에 데이터 저장
        val scheduleRef = databaseReference.child("schedule").child(daySection).child(scheduleTimeId)

        scheduleRef.updateChildren(scheduleDataToUpdate.toMap())
            .addOnSuccessListener {
                Log.d("dbReal", "업데이트 성공")
            }
            .addOnFailureListener {
                Log.d("dbReal", "업데이트 실패")
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

