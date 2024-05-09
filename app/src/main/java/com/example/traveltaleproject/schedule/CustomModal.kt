package com.example.traveltaleproject.schedule

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
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
    private val daySection: String,
    private val travelListId: String,
    private var userId: String
) : Dialog(context) {
    private lateinit var binding: ActivityScheduleModalBinding
    private var scheduleData: ScheduleData? = null
    private val sharedPreferences = context.getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
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
        showToast("$daySection", context)

        // SharedPreferences 초기화
        userId = getSessionId()

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

        if (scheduleData != null && scheduleData?.scheduleTimeId == scheduleTimeId) {
            // If scheduleData is not null and scheduleTimeId matches, it's edit mode
            startTimeSpinner.setSelection(scheduleData?.startTime?.toInt() ?: 0)
            endTimeSpinner.setSelection(scheduleData?.endTime?.toInt() ?: 0)
            scheduleTxtEdit.setText(scheduleData?.scheduleText)
        }

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
                if (scheduleData != null) {
                    // scheduleData가 null이 아니고 scheduleTimeId가 일치하면 수정 모드
                    showToast("일정 업데이트 성공", context)
                    scheduleData?.startTime = selectedStartTime
                    scheduleData?.endTime = selectedEndTime
                    scheduleData?.scheduleText = scheduleText

                    // 기존 데이터를 업데이트하는 함수 호출
                    updateScheduleData(scheduleData!!)
                    scheduleDataListener?.onScheduleDataUpdated(scheduleData!!, scheduleTimeId)
                } else {
                    // 일정 데이터를 생성
                    val scheduleData = createScheduleData(selectedStartTime, selectedEndTime, scheduleText)

                    // saveScheduleDataToDatabase 함수 호출하여 데이터베이스에 저장
                    saveScheduleDataToDatabase(scheduleData)

                    // 인터페이스를 통해 일정 데이터를 fragment로 전달
                    // 수정 버튼을 눌렀을 때 프래그먼트로 데이터 전달하여 fetchScheduleList(daySection) 호출
                    scheduleDataListener?.onScheduleDataReceived(scheduleData!!)

                    // 다이얼로그 닫기
                    dismiss()
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
        scheduleTimeId = UUID.randomUUID().toString()
        this.scheduleTimeId = scheduleTimeId // 클래스 내부의 scheduleTimeId 업데이트
        return ScheduleData(startTime, endTime, scheduleText, scheduleTimeId)
    }

    private fun saveScheduleDataToDatabase(scheduleData: ScheduleData) {
        val databaseReference = FirebaseDatabase.getInstance().reference
            .child("TravelList").child(userId).child(travelListId).child("schedule")

        // 해당 daysection 노드에 새로운 고유한 키로 데이터 추가
        val scheduleRef = databaseReference.child(daySection).child(scheduleTimeId).push()

        scheduleRef.setValue(scheduleData)
            .addOnSuccessListener {
                Log.d("dbReal", "저장 성공")
            }
            .addOnFailureListener {
                Log.d("dbReal", "저장 실패")
            }
    }

    private fun updateScheduleData(scheduleDataToUpdate: ScheduleData) {
        val databaseReference = FirebaseDatabase.getInstance().reference
            .child("TravelList").child(userId).child(travelListId).child("schedule")

        // 해당 daysection 노드에 기존 데이터 업데이트
        val scheduleRef = databaseReference.child(daySection).child(scheduleDataToUpdate.scheduleTimeId)

        scheduleRef.setValue(scheduleDataToUpdate)
            .addOnSuccessListener {
                Log.d("dbReal", "업데이트 성공")
            }
            .addOnFailureListener {
                Log.d("dbReal", "업데이트 실패")
            }
    }

    // 수정된 일정 데이터를 가져오는 메서드 추가
    fun getUpdatedScheduleData(): ScheduleData? {
        return scheduleData
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // 저장 버튼 클릭 리스너 인터페이스
    interface ScheduleDataListener {
        fun onScheduleDataReceived(scheduleData: ScheduleData)
        fun onScheduleDataUpdated(scheduleData: ScheduleData, scheduleTimeId: String)
    }

    private var scheduleDataListener: ScheduleDataListener? = null

    fun setScheduleDataListener(listener: ScheduleDataListener) {
        scheduleDataListener = listener
    }
    private fun getSessionId(): String {
        return sharedPreferences.getString("user_id", "") ?: ""
    }


}

