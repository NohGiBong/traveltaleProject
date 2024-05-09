package com.example.traveltaleproject.schedule

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.traveltaleproject.databinding.ActivityScheduleListBinding
import com.example.traveltaleproject.models.ScheduleData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ScheduleFragment : Fragment(), CustomModal.ScheduleDataListener {

    private lateinit var binding: ActivityScheduleListBinding
    private lateinit var adapter: ScheduleItemAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var dataSet = mutableListOf<ScheduleData>()
    private lateinit var travelListId: String
    private lateinit var userId: String

    // 수정된 데이터를 전달하기 위한 리스너
    private var scheduleDataListener: CustomModal.ScheduleDataListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityScheduleListBinding.inflate(inflater, container, false)
        val view = binding.root
        val recyclerView = binding.scheduleItem
        val scheduleAddButton = binding.submitBtn
        val daySection = arguments?.getString("daySection")

        sharedPreferences = requireContext().getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
        userId = getSessionId()

        // RecyclerView 및 Adapter 초기화
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        // 어댑터 초기화
        adapter = ScheduleItemAdapter(requireContext(), dataSet, userId, travelListId, daySection ?: "")
        recyclerView.adapter = adapter

        // 저장 버튼 클릭 이벤트 설정
        scheduleAddButton.setOnClickListener {
            // 다이얼로그 보여주기
            val dialog = CustomModal(requireContext(), daySection ?: "", travelListId, userId)
            dialog.show()

            // 모달창에서 받은 데이터를 처리하기 위해 프래그먼트 자체를 리스너로 설정
            dialog.setScheduleDataListener(this@ScheduleFragment)
        }

        // 스케줄 데이터 불러오기
        fetchScheduleList(daySection)

        // 수정된 데이터를 전달하기 위한 리스너 설정
        scheduleDataListener = object : CustomModal.ScheduleDataListener {
            override fun onScheduleDataReceived(scheduleData: ScheduleData) {
                // 받은 일정 데이터를 dataSet에 추가하고 어댑터에 알림
                dataSet.add(scheduleData)
                adapter.notifyItemInserted(dataSet.size - 1)
            }

            override fun onScheduleDataUpdated(scheduleData: ScheduleData, scheduleTimeId: String) {
                // 받은 일정 데이터를 dataSet에서 찾아 업데이트하고 어댑터에 알림
                val index = dataSet.indexOfFirst { it.scheduleTimeId == scheduleTimeId }
                Toast.makeText(context, scheduleTimeId, Toast.LENGTH_SHORT).show()
                if (index != -1) {
                    dataSet[index] = scheduleData
                    adapter.updateDataItem(scheduleData)

                    // 프래그먼트로 수정된 데이터와 함께 콜백 호출
                    scheduleDataListener?.onScheduleDataUpdated(scheduleData, scheduleTimeId)
                }
            }
        }

        return view
    }

    // 스케줄 데이터 불러오는 메서드 수정
    private fun fetchScheduleList(daySection: String?) {
        daySection?.let {
            FirebaseDatabase.getInstance().reference
                .child("TravelList").child(userId).child(travelListId).child("schedule").child(it)
        }?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val scheduleList = mutableListOf<ScheduleData>()

                    // 데이터베이스에서 스케줄 데이터를 가져와서 dataSet에 추가
                    for (childSnapshot in snapshot.children) {
                        val startTime = childSnapshot.child("startTime").getValue(Long::class.java)
                        val endTime = childSnapshot.child("endTime").getValue(Long::class.java)
                        val scheduleText = childSnapshot.child("scheduleText").getValue(String::class.java)

                        // startTime이 null이 아닌 경우에만 처리
                        startTime?.let {
                            // childSnapshot의 키를 scheduleTimeId로 사용
                            val scheduleItem = if (endTime != null) {
                                ScheduleData(it, endTime, scheduleText, childSnapshot.key ?: "")
                            } else {
                                null // endTime이 null이면 null을 반환
                            }

                            scheduleItem?.let {
                                scheduleList.add(it) // null이 아닌 경우에만 scheduleList에 추가
                            }
                        }
                    }

                    // 어댑터에 변경사항 알리기
                    adapter.setData(scheduleList)
                } else {
                    // 데이터가 없는 경우 처리
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 오류 처리
            }
        })
    }

    // 모달 창에서 새로운 일정 데이터를 받았을 때 호출되는 콜백 함수
    override fun onScheduleDataReceived(scheduleData: ScheduleData) {
        // 받은 일정 데이터를 dataSet에 추가하고 어댑터에 알림
        dataSet.add(scheduleData)
        adapter.notifyItemInserted(dataSet.size - 1)
    }

    // 모달 창에서 수정된 일정 데이터를 받았을 때 호출되는 콜백 함수
    override fun onScheduleDataUpdated(scheduleData: ScheduleData, scheduleTimeId: String) {
        // 받은 일정 데이터를 dataSet에서 찾아 업데이트하고 어댑터에 알림
        val index = dataSet.indexOfFirst { it.scheduleTimeId == scheduleTimeId }
        Toast.makeText(context, scheduleTimeId, Toast.LENGTH_SHORT).show()
        if (index != -1) {
            dataSet[index] = scheduleData
            adapter.notifyItemChanged(index)

            // 프래그먼트로 수정된 데이터와 함께 콜백 호출
            scheduleDataListener?.onScheduleDataUpdated(scheduleData, scheduleTimeId)
        }
    }

    private fun getSessionId(): String {
        return sharedPreferences.getString("user_id", "").toString()
    }

    companion object {
        // newInstance 메서드 정의
        fun newInstance(selectedDate: String, travelListId: String, userId: String): ScheduleFragment {
            val args = Bundle().apply {
                putString("selectedDate", selectedDate)
                putString("travelListId", travelListId)
                putString("userId", userId)
            }
            val fragment = ScheduleFragment()
            fragment.arguments = args
            fragment.travelListId = travelListId
            return fragment
        }
    }
}

