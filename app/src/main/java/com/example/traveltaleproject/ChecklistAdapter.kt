package com.example.traveltaleproject

import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChecklistAdapter(private val dataList: MutableList<String>) :
    RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder>() {

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_checklist_item, parent, false)
        return ChecklistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChecklistViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    inner class ChecklistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardView: CardView = view.findViewById(R.id.checklist_item_edit)
        private val editText: EditText = cardView.findViewById(R.id.chk_txt_edit)
        private val chkImgEdit: ImageView = cardView.findViewById(R.id.chk_img_edit)
        private val chkDelEdit: ImageView = cardView.findViewById(R.id.chk_del_edit)

        // 체크 이미지 변경
        var isChecked = false

        // 수정
        var isEditMode = false


        init {
            // chkImgEdit 클릭 시 unchecked -> checked 변경
            chkImgEdit.setOnClickListener {
                Log.d("ChecklistAdapter", "체크 이미지 클릭 !")

                isChecked = !isChecked // 상태를 변경

                // isChecked에 따라 이미지 변경
                if (isChecked) {
                    chkImgEdit.setImageResource(R.drawable.checked)
                } else {
                    chkImgEdit.setImageResource(R.drawable.unchecked)
                }
            }

            // editText 입력 후 엔터 -> db에 저장
            editText.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    // 엔터 키가 입력되었을 때 실행되는 로직
                    val newItemText = editText.text.toString()
                    saveNewItemToDatabase(newItemText)
                    true
                } else {
                    false
                }
            }

            // 수정
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && !isEditMode) {
                    enterEditMode()
                }
            }

            editText.setOnEditorActionListener {  _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    exitEditMode()
                    true
                } else {
                    false
                }

            }


            // chkdeledit 클릭 시 recyclerview와 db 삭제
            chkDelEdit.setOnClickListener {
                val database = Firebase.database
                val position = adapterPosition // 현재 아이템의 위치를 가져옴

                val snackbar = Snackbar.make(itemView, "정말로 삭제하시겠습니까?", Snackbar.LENGTH_LONG)
                    .setAction("삭제") {
                        if (position != RecyclerView.NO_POSITION) {
                            if (dataList.isNotEmpty() && position < dataList.size) {
                                // Firebase Realtime Database에서 아이템 삭제
                                val itemId = dataList[position] // dataList에서 해당 위치의 아이템을 가져옴
                                val itemRef = database.getReference("check").child(itemId)
                                itemRef.removeValue()
                                    .addOnSuccessListener {
                                        println("db 삭제 성공")

                                        // RecyclerView에서 아이템 제거
                                        dataList.removeAt(position)
                                        notifyItemRemoved(position)
                                    }
                                    .addOnFailureListener { exception ->
                                        println("db 삭제 실패 : $exception")
                                    }
                            } else {
                                Log.e("ChecklistAdapter", "datalist x")
                            }
                        }
                    }
                snackbar.show()
            }
        }

        fun bind(text: String) {
            editText.setText(text)
        }

        // Firebase Realtime Database 인스턴스 가져오기
        val database = Firebase.database

        // 새로운 아이템을 저장하는 함수
        fun saveNewItemToDatabase(newItemText: String) {
            // "checklist" 노드에 새로운 아이템 추가
            val newItemRef = database.getReference("check").push()
            newItemRef.setValue(newItemText)
                .addOnSuccessListener {
                    println("New item saved to database")
                }
                .addOnFailureListener { exception ->
                    println("Error saving new item to database: $exception")
                }
        }

        fun enterEditMode() {
            isEditMode = true
            editText.requestFocus()
        }

        fun exitEditMode() {
            isEditMode = false
            val newText = editText.text.toString()
            updateItem(newText)
        }

        fun updateItem(newText: String) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                // 이전 아이템 키 가져오기
                val oldItemId = dataList[position]
                // Firebase Realtime Database에서 해당 아이템의 레퍼런스 가져오기
                val itemRef = database.getReference("check").child(oldItemId)
                // 새로운 값을 HashMap으로 저장
                val updateData = HashMap<String, Any>()
                updateData["text"] = newText
                // Firebase Realtime Database에 데이터 업데이트
                itemRef.updateChildren(updateData)
                    .addOnSuccessListener {
                        Log.d("edit", "db 수정 성공")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("edit", "db 수정 실패 : $exception")
                    }
            }
        }
    }
}
