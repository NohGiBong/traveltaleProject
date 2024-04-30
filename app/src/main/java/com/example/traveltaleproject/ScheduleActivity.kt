package com.example.traveltaleproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.databinding.ActivityScheduleBinding

class ScheduleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
