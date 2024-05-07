package com.example.traveltaleproject.checklist

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.BottomNavigationHelper
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityChecklistBinding
import com.example.traveltaleproject.models.Check
import com.example.traveltaleproject.utils.SwipeToDeleteCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.util.UUID

class CheckListActivity : AppCompatActivity() {
    private lateinit var bottomNavigationHelper: BottomNavigationHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CheckListAdapter
    private lateinit var travelListId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        userId = getSessionId()
        travelListId = intent.getStringExtra("travelListId") ?: ""

        // ChecklistAdapter 초기화
        adapter = CheckListAdapter(mutableListOf(), userId, travelListId)
        recyclerView.adapter = adapter

        // 데이터 가져오기
        fetchCheckListData()

        // submitBtn 클릭 시 checklist_item 출력
        binding.submitBtn.setOnClickListener {
            val newItem = Check(UUID.randomUUID().toString(), "")

            adapter.addData(newItem)

            Toast.makeText(this, "입력 후 스페이스바 + 엔터를 눌러주세요", Toast.LENGTH_LONG).show()
        }

        // BottomNavigationHelper 초기화
        bottomNavigationHelper = BottomNavigationHelper(this, this)

        // 네비게이션 뷰의 아이템 선택 리스너 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationHelper.setupBottomNavigationListener(bottomNavigationView)

        // 빈 아이템 스와이프 삭제 기능 추가
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    // CheckList 데이터 뿌리기
    private fun fetchCheckListData() {
        val database = Firebase.database
        val checklistRef = database.getReference("check").child(userId).child(travelListId)

        checklistRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val checklistItems = mutableListOf<Check>()

                for (itemSnapshot in snapshot.children) {
                    val checkId = itemSnapshot.key ?: ""
                    val text = itemSnapshot.child("text").getValue(String::class.java) ?: ""
                    val status = itemSnapshot.child("status").getValue(String::class.java) ?: "unchecked"
                    val checkItem = Check(checkId, text, status)
                    checklistItems.add(checkItem)
                }

                // 어댑터에 데이터 설정
                adapter.setData(checklistItems)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "Failed to fetch checklist data: $error")
            }
        })
    }

    private fun getSessionId() : String {
        return sharedPreferences.getString("user_id", "").toString()
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

