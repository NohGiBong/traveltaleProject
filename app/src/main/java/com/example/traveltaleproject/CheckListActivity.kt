package com.example.traveltaleproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.databinding.ActivityChecklistBinding
class CheckListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val datalist = mutableListOf<String>()
        val adapter = ChecklistAdapter(datalist)
        recyclerView.adapter = adapter

        // submitBtn 클릭 시 checklist_item 출력
        binding.submitBtn.setOnClickListener {
            val newItem = ""

            datalist.add(newItem)

            adapter.notifyItemInserted(datalist.size -1)
        }
    }
}
