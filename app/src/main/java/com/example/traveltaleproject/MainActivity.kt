package com.example.traveltaleproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.databinding.ActivityMainBinding
import com.example.traveltaleproject.databinding.ActivityScheduleBinding
import com.google.firebase.Firebase
import com.google.firebase.database.database

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.startBtn.setOnClickListener {
//            val intent = Intent(this@MainActivity, LoginActivity::class.java)
//            startActivity(intent)
//        }
    }
}