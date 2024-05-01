package com.example.traveltaleproject

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.databinding.ActivityChecklistItemBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// 데이터베이스 설정
private val database = Firebase.database
private val myRef = database.getReference().child("check")

class ChecklistViewHolder(val binding: ActivityChecklistItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: String) {
        binding.chkTxtEdit.setText(item)

        // 이미지 클릭 시 unchecked -> checked 바꿈
        binding.chkImgEdit.setOnClickListener {
            // 현재 ImageView의 이미지 리소스 가져옴
            val currentDrawable = binding.chkImgEdit.drawable.constantState

            if (currentDrawable == ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.unchecked
                )?.constantState
            ) {
                binding.chkImgEdit.setImageResource(R.drawable.checked)
            } else {
                binding.chkImgEdit.setImageResource(R.drawable.unchecked)
            }
        }
    }
}

class ChecklistAdapter(val dataList: MutableList<String>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return dataList?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ChecklistViewHolder(
            ActivityChecklistItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ChecklistViewHolder).binding

        binding.chkTxtEdit.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                // Enter 키가 눌렸을 때 새로운 아이템을 추가할 데이터 준비
                val newItemText = binding.chkTxtEdit.text.toString()

                // 데이터베이스에 새로운 아이템 저장
                saveNewItemToDatabase(newItemText)

                // RecyclerView 어댑터에 데이터가 변경되었음을 알림
                notifyDataSetChanged()

                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }

    private fun saveNewItemToDatabase(newItemText: String) {
        // 데이터베이스에 새로운 아이템 저장
        val newItemKey = myRef.push().key // 새로운 아이템의 고유한 키 생성
        newItemKey?.let { key ->
            myRef.child(key).setValue(newItemText)
                .addOnSuccessListener {
                    // 데이터베이스에 성공적으로 저장되면 로그를 출력합니다.
                    println("New item saved to database")
                }
                .addOnFailureListener { exception ->
                    // 저장에 실패하면 오류 메시지를 출력합니다.
                    println("Error saving new item to database: $exception")
                }
        }
    }
}
