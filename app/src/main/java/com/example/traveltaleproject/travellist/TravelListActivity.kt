package com.example.traveltaleproject.travellist

import android.content.Context
import android.content.Intent
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
import com.example.traveltaleproject.databinding.ActivityTravellistBinding
import com.example.traveltaleproject.models.TravelList
import com.example.traveltaleproject.utils.SwipeToDeleteCallbackTravelList
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TravelListActivity : AppCompatActivity() {
    private lateinit var bottomNavigationHelper: BottomNavigationHelper
    private lateinit var binding: ActivityTravellistBinding
    private lateinit var adapter: TravelListAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTravellistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyInfo", Context.MODE_PRIVATE)
        userId = getSessionId()

        databaseReference = FirebaseDatabase.getInstance().reference.child("TravelList").child(userId ?: "")

        binding.itemAddBtn.setOnClickListener {
            val intent = Intent(this, TravelAddActivity::class.java)
            startActivity(intent)
        }

        recyclerView = binding.listRecyclerview // recyclerView 변수 초기화
        recyclerView.layoutManager = LinearLayoutManager(this)

        binding.listRecyclerview.layoutManager = LinearLayoutManager(this)

        adapter = TravelListAdapter(this, mutableListOf(), userId)
        binding.listRecyclerview.adapter = adapter

        // 데이터 가져오기
        fetchTravelList()

        // BottomNavigationHelper 초기화
        bottomNavigationHelper = BottomNavigationHelper(this, this)

        // 네비게이션 뷰의 아이템 선택 리스너 설정
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationHelper.setupBottomNavigationListener(bottomNavigationView)

        // 아이템 스와이프 삭제 기능 추가
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallbackTravelList(adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun fetchTravelList() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val travelList = mutableListOf<TravelList>()
                for (data in snapshot.children) {
                    val travel = data.getValue(TravelList::class.java)
                    travel?.let {
                        travelList.add(it)
                    }
                }
                adapter.setList(travelList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TravelListActivity", "Error fetching travel list: ${error.message}")
            }
        })
    }

    private fun getSessionId(): String {
        return sharedPreferences.getString("user_id", "").toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
