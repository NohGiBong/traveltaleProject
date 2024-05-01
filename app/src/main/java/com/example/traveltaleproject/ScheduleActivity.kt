package com.example.traveltaleproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScheduleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleItemAdapter
    private lateinit var cardView: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        recyclerView = findViewById(R.id.schedule_item)
        adapter = ScheduleItemAdapter()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        cardView = findViewById(R.id.schedule_item_add)
        cardView.setOnClickListener {
            addItem()
        }
    }

    private fun addItem() {
        val newItem = "New Item"
        adapter.addItem(newItem)
    }
}