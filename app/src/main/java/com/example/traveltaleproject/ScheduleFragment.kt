package com.example.traveltaleproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.traveltaleproject.databinding.ActivityScheduleListBinding

class ScheduleFragment : Fragment() {
    private lateinit var binding: ActivityScheduleListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 바인딩 초기화
        binding = ActivityScheduleListBinding.inflate(inflater, container, false)

        // 바인딩의 root 뷰 반환
        return binding.root
    }
}
