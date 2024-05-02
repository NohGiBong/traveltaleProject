package com.example.traveltaleproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.traveltaleproject.databinding.ActivityChecklistBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CheckListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChecklistBinding
    private var dataList: MutableList<String>? = null
    private lateinit var adapter: ChecklistAdapter
    private lateinit var bottomNavigationHelper: BottomNavigationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // dataList 초기화
        dataList =
            savedInstanceState?.getStringArrayList("datalist")?.toMutableList() ?: mutableListOf()

        // RecyclerView 설정
        setupRecyclerView()

        // submit_btn 클릭 리스너 설정
        binding.submitBtn.setOnClickListener {
            // 새로운 아이템을 추가할 데이터 준비
            val newItemText = ""

            // 데이터를 RecyclerView 어댑터의 리스트에 추가
            dataList?.add(newItemText)

            // RecyclerView 어댑터에 데이터가 변경되었음을 알림
            adapter.notifyDataSetChanged()
        }

        // BottomNavigationHelper 초기화
        bottomNavigationHelper = BottomNavigationHelper(this, this)

        // 네비게이션 뷰의 아이템 선택 리스너 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationHelper.setupBottomNavigationListener(bottomNavigationView)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerview.layoutManager = layoutManager
        adapter = ChecklistAdapter(dataList)
        binding.recyclerview.adapter = adapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("datalist", ArrayList(dataList))
    }
}
