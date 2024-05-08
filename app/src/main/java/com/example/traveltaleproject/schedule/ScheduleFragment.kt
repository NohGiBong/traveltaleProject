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

        // 데이터셋 초기화
        dataSet = mutableListOf()

        // 어댑터 초기화
        adapter = ScheduleItemAdapter(requireContext(), dataSet)
        recyclerView.adapter = adapter

        // 저장 버튼 클릭 이벤트 설정
        scheduleAddButton.setOnClickListener {
            // 다이얼로그 보여주기
            val dialog =
                daySection?.let { daySection -> CustomModal(requireContext(), daySection, travelListId, userId) }

            dialog?.show()

            dialog?.setScheduleDataListener(object : CustomModal.ScheduleDataListener {
                override fun onScheduleDataReceived(scheduleData: ScheduleData) {
                    // 스케줄 데이터를 받아와서 처리
                    // 예를 들어, 데이터셋에 추가하고 어댑터에 알림
                    dataSet.add(scheduleData)

                    // 리사이클러뷰의 스크롤 위치를 마지막으로 이동
                    recyclerView.scrollToPosition(dataSet.size - 1)
                }
            })
        }

        fetchScheduleList(daySection)

        return view
    }

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
                        val scheduleText =
                            childSnapshot.child("scheduleText").getValue(String::class.java)

                        showToast("$startTime")
                        // startTime이 null이 아닌 경우에만 처리
                        startTime?.let {
                            val scheduleItem = if (endTime != null) {
                                ScheduleData(it, endTime, scheduleText)
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


    private fun showToast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onScheduleDataReceived(scheduleData: ScheduleData) {
        // 새로운 데이터를 데이터셋에 추가하고 어댑터에 알림
        dataSet.add(scheduleData)
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
