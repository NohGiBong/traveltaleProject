package com.example.traveltaleproject

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.databinding.ActivityListBinding

class ListActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}