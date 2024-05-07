package com.example.traveltaleproject.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.viewbinding.ViewBinding
import com.example.traveltaleproject.databinding.ActivityRegisterBinding

class PhoneTextWatcher(private val binding: ViewBinding) : TextWatcher {
    private var isFormatting: Boolean = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (!isFormatting) {
            isFormatting = true
            val formattedText = formatPhoneNumber(s.toString())

            // Reflection을 사용하여 필드 이름 동적으로 설정
            val fieldName = if (binding is ActivityRegisterBinding) "regiPhone" else "myinfoPhone"

            // 해당 필드에 접근하여 EditText를 가져온 후 문자열 설정
            val editTextField = binding::class.java.getDeclaredField(fieldName).apply {
                isAccessible = true
            }
            val editText = editTextField.get(binding) as EditText
            editText.setText(formattedText)

            // 커서 위치 설정
            editText.setSelection(formattedText.length)

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