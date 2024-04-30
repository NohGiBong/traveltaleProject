package com.example.traveltaleproject

import android.text.Editable
import android.text.TextWatcher
import com.example.traveltaleproject.databinding.ActivityRegisterBinding

class PhoneTextWatcher(private val binding: ActivityRegisterBinding) : TextWatcher {
    private var isFormatting: Boolean = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (!isFormatting) {
            isFormatting = true
            val formattedText = formatPhoneNumber(s.toString())
            binding.regiPhone.setText(formattedText)
            binding.regiPhone.setSelection(formattedText.length)
            isFormatting = false
        }
    }

    override fun afterTextChanged(s: Editable?) {}

    private fun formatPhoneNumber(phoneNumber: String): String {
        var formattedPhoneNumber = phoneNumber.replace("-", "")
        if (formattedPhoneNumber.length in 3..6) {
            formattedPhoneNumber = formattedPhoneNumber.substring(0, 3) + "-" + formattedPhoneNumber.substring(3)
        } else if (formattedPhoneNumber.length >= 7) {
            formattedPhoneNumber =
                formattedPhoneNumber.substring(0, 3) + "-" + formattedPhoneNumber.substring(3, 7) + "-" + formattedPhoneNumber.substring(7)
        }
        return formattedPhoneNumber
    }
}