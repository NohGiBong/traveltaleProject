package com.example.traveltaleproject.checklist

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.example.traveltaleproject.databinding.ActivityChecklistEditmodalBinding

class ChecklistEditModal(context: Context, private val editListener: () -> Unit) : Dialog(context) {
    private lateinit var binding: ActivityChecklistEditmodalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistEditmodalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(true)
        setCancelable(true)

        binding.checkCancle1.setOnClickListener {
            dismiss()
        }

        binding.checkEdit.setOnClickListener {
            editListener()
            dismiss()
        }
    }
}
