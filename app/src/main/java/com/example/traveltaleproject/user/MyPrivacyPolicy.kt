package com.example.traveltaleproject.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.traveltaleproject.databinding.ActivityPrivacyBinding

class MyPrivacyPolicy : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closePrivacy.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}