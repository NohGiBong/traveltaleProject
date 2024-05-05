package com.example.traveltaleproject.schedule

import CustomModal
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.R

class ScheduleFragment : Fragment(), CustomModal.ScheduleDataListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleItemAdapter
    private var dataSet = mutableListOf<CustomModal.ScheduleData>()

    companion object {
        // newInstance 메서드 정의
        fun newInstance(selectedDate: String): ScheduleFragment {
            val args = Bundle().apply {
                putString("selectedDate", selectedDate)
            }
            val fragment = ScheduleFragment()
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_schedule_list, container, false)
        recyclerView = view.findViewById(R.id.schedule_item)
        val scheduleAddButton = view.findViewById<RelativeLayout>(R.id.submit_btn)

        // RecyclerView 및 Adapter 초기화
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        // 데이터셋 초기화
        dataSet = mutableListOf()

        // 어댑터 초기화
        adapter = ScheduleItemAdapter(context, dataSet)
        recyclerView.adapter = adapter

        // 저장 버튼 클릭 이벤트 설정
        scheduleAddButton.setOnClickListener {
            // 다이얼로그 보여주기
            val dialog = CustomModal(requireContext())
            dialog.show()

            dialog.setScheduleDataListener(object : CustomModal.ScheduleDataListener {
                override fun onScheduleDataReceived(scheduleData: CustomModal.ScheduleData) {
                    // 스케줄 데이터를 받아와서 처리
                    // 예를 들어, 데이터셋에 추가하고 어댑터에 알림
                    dataSet.add(scheduleData)

                    // 리사이클러뷰의 스크롤 위치를 마지막으로 이동
                    recyclerView.scrollToPosition(dataSet.size - 1)
                }
            })
        }

        return view
    }

    override fun onScheduleDataReceived(scheduleData: CustomModal.ScheduleData) {
        // 새로운 데이터를 데이터셋에 추가하고 어댑터에 알림
        dataSet.add(scheduleData)
    }
}

