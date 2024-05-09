package com.example.traveltaleproject.checklist

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.example.traveltaleproject.databinding.ActivityChecklistRemovemodalBinding

class CheckListRemoveModal(context: Context, private val removeListener: () -> Unit) : Dialog(context) {
    private lateinit var binding: ActivityChecklistRemovemodalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistRemovemodalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(true)
        setCancelable(true)

        binding.checkCancle2.setOnClickListener {
            dismiss()
        }

        binding.checkRemove.setOnClickListener {
            // 수정 리스너 호출
            removeListener()
            dismiss()
        }
    }
}