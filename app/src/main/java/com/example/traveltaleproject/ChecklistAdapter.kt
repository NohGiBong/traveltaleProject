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

        var isChecked = false

        init {
            // chkImgEdit 클릭 시 unchecked -> checked 변경
            chkImgEdit.setOnClickListener {
                Log.d("ChecklistAdapter", "Image clicked!")

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

            // chkdeledit 클릭 시 recyclerview와 db 삭제
            chkDelEdit.setOnClickListener {
                val snackbar = Snackbar.make(itemView, "정말로 삭제하시겠습니까?", Snackbar.LENGTH_LONG)
                    .setAction("삭제") {
                        val position = adapterPosition // 현재 아이템의 위치를 가져옴
                        if (position != RecyclerView.NO_POSITION) {
                            if (dataList.isNotEmpty() && position < dataList.size) {
                                // Firebase Realtime Database에서 아이템 삭제
                                val database = Firebase.database
                                val itemId = dataList[position] // dataList에서 해당 위치의 아이템을 가져옴
                                val itemRef =
                                    database.getReference("check").child(itemId) // Firebase 경로를 생성함
                                itemRef.removeValue()
                                    .addOnSuccessListener {
                                        println("Item removed from database")

                                        // RecyclerView에서 아이템 제거
                                        dataList.removeAt(position)
                                        notifyItemRemoved(position)
                                    }
                                    .addOnFailureListener { exception ->
                                        println("Error removing item from database: $exception")
                                    }
                            } else {
                                Log.e("ChecklistAdapter", "삭제!!!!")
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
    }
}
