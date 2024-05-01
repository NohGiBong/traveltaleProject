package com.example.traveltaleproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.databinding.ActivityChecklistBinding

class CheckListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChecklistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}
