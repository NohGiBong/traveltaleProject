package com.example.traveltaleproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.databinding.ActivityTaleWriteBinding

class TaleWriteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityTaleWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}