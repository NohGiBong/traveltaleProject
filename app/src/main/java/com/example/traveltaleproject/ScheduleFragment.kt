package com.example.traveltaleproject

import CustomModal
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView


class ScheduleFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: ScheduleItemAdapter

    companion object {
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
        // 레이아웃 인플레이션
        val view = inflater.inflate(R.layout.activity_schedule_list, container, false)
        val schedule_add = view.findViewById<RelativeLayout>(R.id.submit_btn)

        schedule_add.setOnClickListener {
            schedule_add.setOnClickListener {
               val dialog = CustomModal(requireContext())
                dialog.show()
            }
        }

        // 리사이클러뷰 초기화
//        recyclerView = view.findViewById(R.id.schedule_item)
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

//    override fun onResume() {
//        super.onResume()
//
//        // 어댑터 초기화
//        val scheduleList = mutableListOf<String>()
//        for (i in 0..23) {
//            if(i < 12) {
//                if(i < 10) {
//                    scheduleList.add("0$i AM")
//                } else {
//                    scheduleList.add("$i AM")
//                }
//            } else {
//                scheduleList.add("$i PM")
//            }
//        }
//        adapter = ScheduleItemAdapter(scheduleList)
//        recyclerView.adapter = adapter
//    }
}


