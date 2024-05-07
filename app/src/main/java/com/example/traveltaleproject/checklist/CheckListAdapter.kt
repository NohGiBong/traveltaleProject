package com.example.traveltaleproject.checklist

import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.models.Check
import com.example.traveltaleproject.R
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.UUID

class CheckListAdapter(private val dataList: MutableList<Check>, private val userId : String, private val travelListId: String?) :
    RecyclerView.Adapter<CheckListAdapter.ChecklistViewHolder>() {

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_checklist_item, parent, false)
        return ChecklistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChecklistViewHolder, position: Int) {
        holder.bind(dataList[position], position)
    }

    inner class ChecklistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val checkListItemWhole:RelativeLayout = view.findViewById(R.id.checklist_item_whole)
        private val checkListItem: RelativeLayout = checkListItemWhole.findViewById(R.id.checklist_item_item)
        private val editText: EditText = checkListItem.findViewById(R.id.chk_txt_edit)
        private val chkImgEdit: ImageView = checkListItem.findViewById(R.id.chk_img_edit)
        private val moreBtn:ImageButton = checkListItem.findViewById(R.id.more_btn)
        private val checkListRecyclerViewEdit: RelativeLayout = checkListItemWhole.findViewById(R.id.checklist_recyclerview_edit)
        private val editBtn : ImageButton = checkListRecyclerViewEdit.findViewById(R.id.chk_item_edit_btn)
        private val deleteBtn : ImageButton = checkListRecyclerViewEdit.findViewById(R.id.chk_item_del_btn)
        private lateinit var checkId: String

        init {

            // 스와이프하여 아이템 삭제
            checkListItem.setOnTouchListener { _, _ ->
                removeEmptyItem(adapterPosition)
                true
            }

            // 뷰단 : chkImgEdit 클릭 시 unchecked -> checked 변경
            var isChecked = false
            chkImgEdit.setOnClickListener {
                Log.d("ChecklistAdapter", "체크 이미지 클릭 !")

                isChecked = !isChecked // 상태를 변경

                if (isChecked) {
                    chkImgEdit.setImageResource(R.drawable.checked)
                } else {
                    chkImgEdit.setImageResource(R.drawable.unchecked)
                }

                val status = if (isChecked) "checked" else "unchecked"

                updateItemCheckedStatus(status, adapterPosition)
            }

            // 아이템 항목 more 버튼 (수정, 삭제 버튼 노출)
            moreBtn.setOnClickListener {
                if (checkListRecyclerViewEdit.visibility == View.GONE) {
                    checkListRecyclerViewEdit.visibility = View.VISIBLE
                    chkImgEdit.isClickable = false
                    editText.isEnabled = false
                    moreBtn.isClickable = false
                } else {
                    checkListRecyclerViewEdit.visibility = View.GONE
                    chkImgEdit.isClickable = true
                    editText.isEnabled = true
                    moreBtn.isClickable = true
                }
            }

            checkListRecyclerViewEdit.setOnClickListener {
                if (checkListRecyclerViewEdit.visibility == View.VISIBLE) {
                    checkListRecyclerViewEdit.visibility = View.GONE
                    chkImgEdit.isClickable = true
                    editText.isEnabled = true
                    moreBtn.isClickable = true
                }
            }

            // editText 입력 후 엔터 -> db에 저장
            editText.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    val newItemText = editText.text.toString()
                    saveNewItemToDatabase(newItemText)
                    true
                } else {
                    false
                }
            }
        }

        fun bind(check: Check, position: Int) {
            editText.setText(check.text)
            checkId = check.checkid

            editBtn.setOnClickListener {
                if (checkListRecyclerViewEdit.visibility == View.VISIBLE) {
                    checkListRecyclerViewEdit.visibility = View.GONE
                    chkImgEdit.isClickable = true
                    editText.isEnabled = true
                    moreBtn.isClickable = true
                }
                updateItemText(editText.text.toString(), position)
            }
            deleteBtn.setOnClickListener {
                deleteItem(check.checkid)
            }

            chkImgEdit.isEnabled = editText.text.toString().isNotEmpty()

            // 체크 이미지 바인딩
            if (check.status == "checked") {
                chkImgEdit.setImageResource(R.drawable.checked)
            } else {
                chkImgEdit.setImageResource(R.drawable.unchecked)
            }
        }
    }

    // RealTime DB : CheckList 저장
    private fun saveNewItemToDatabase(newItemText: String) {
        val database = Firebase.database
        val newItemRef = database.getReference("check").child(userId).child(travelListId ?: "")

        val newItemKey = UUID.randomUUID().toString()

        val newItem = Check(newItemKey, newItemText)

        newItemRef.child(newItemKey).setValue(newItem)
            .addOnSuccessListener {
                println("db 추가 성공")
            }
            .addOnFailureListener { exception ->
                println("db 추가 실패: $exception")
            }
    }

    // RealTime DB : CheckList 수정
    private fun updateItemText(newText: String, position: Int) {
        val checkId = dataList[position].checkid

        val database = Firebase.database
        val itemRef = database.getReference("check").child(userId).child(travelListId ?: "").child(checkId)

        val updates: MutableMap<String, Any> = HashMap()
        updates["text"] = newText

        itemRef.updateChildren(updates)
            .addOnSuccessListener {
                Log.d("certain", "업데이트 완료")
            }
            .addOnFailureListener { exception ->
                Log.e("ChecklistAdapter", "아이템의 텍스트 업데이트 실패: $exception")
                Log.d("certain", "업데이트 실패 $exception")
            }
    }

    // RealTime DB : CheckList 삭제
    private fun deleteItem(checkId: String) {
        val database = Firebase.database
        val itemRef = database.getReference("check").child(userId).child(travelListId?: "").child(checkId)

        itemRef.removeValue()
            .addOnSuccessListener {
                Log.d("certain", "아이템 삭제 완료")
            }
            .addOnFailureListener { exception ->
                Log.e("ChecklistAdapter", "아이템 삭제 실패: $exception")
            }
    }

    // RealTime DB : CheckList Check 상태 수정
    private fun updateItemCheckedStatus(status: String, position: Int) {
        val checkId = dataList[position].checkid

        val database = Firebase.database
        val itemRef = database.getReference("check").child(userId).child(travelListId?: "").child(checkId)

        val updates: MutableMap<String, Any> = HashMap()
        updates["status"] = status

        itemRef.updateChildren(updates)
            .addOnSuccessListener {
                Log.d("certain", "상태값 업데이트 완료")
            }
            .addOnFailureListener { exception ->
                Log.e("ChecklistAdapter", "상태값 업데이트 실패: $exception")
            }
    }

    // 비어있는 Item만 스와이프 허용
    fun canSwipeItem(position: Int): Boolean {
        return position >= 0 && position < dataList.size && dataList[position].text.isEmpty()
    }

    // 비어있는 Item 스와이프로 삭제 기능
    fun removeEmptyItem(position: Int) {
        dataList.removeAt(position)
        notifyItemRemoved(position)
    }

    // 데이터 추가 메서드
    fun addData(newItem: Check) {
        dataList.add(newItem)
        notifyDataSetChanged()
    }

    // 데이터 설정 메서드
    fun setData(newDataList: MutableList<Check>) {
        dataList.clear()
        dataList.addAll(newDataList)
        notifyDataSetChanged()
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}