package com.example.traveltaleproject.checklist

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.cardview.widget.CardView
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
                    saveNewItemToDatabase(newItemText, checkId)
                    true
                } else {
                    false
                }
            }
        }

        fun bind(check: Check, position: Int) {
            editText.setText(check.text)
            checkId = check.checkid

            // 수정 이미지 버튼 클릭 시 이벤트
            editBtn.setOnClickListener {
                showEditModal(itemView.context, editText, position, checkId)
            }

            // 삭제 이미지 버튼 클릭 시 이벤트
            deleteBtn.setOnClickListener {
                val deleteModal = ChecklistRemoveModal(itemView.context) {
                    deleteItem(check.checkid, position)
                }
                deleteModal.show()
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

    // RealTime DB : CheckList 추가
    private fun saveNewItemToDatabase(newItemText: String, checkId: String?) {
        val database = Firebase.database
        val newItemRef = database.getReference("TravelList").child(userId).child(travelListId ?: "").child("check")

        val newItemKey = checkId ?: UUID.randomUUID().toString() // 기존 checkId가 없으면 새로 생성

        val newItem = Check(newItemKey, newItemText)

        newItemRef.child(newItemKey).setValue(newItem)
            .addOnSuccessListener {
                println("db 추가 성공")
            }
            .addOnFailureListener { exception ->
                println("db 추가 실패: $exception")
            }
    }

    // 수정 모달
    private fun showEditModal(context: Context, editText: EditText, position: Int, checkId: String) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView: View = inflater.inflate(R.layout.activity_checklist_editmodal, null)
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialog.setView(dialogView)
        val alertDialogInstance = alertDialog.create()
        alertDialogInstance.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialogInstance.show()

        val cancelBtn: CardView = dialogView.findViewById(R.id.check_cancle1)
        val editBtn: CardView = dialogView.findViewById(R.id.check_edit)

        cancelBtn.setOnClickListener {
            alertDialogInstance.dismiss()
        }

        editBtn.setOnClickListener {
            val newText = editText.text.toString()
            if (checkId.isNotEmpty()) {
                updateItemText(newText, checkId)
            } else {
                saveNewItemToDatabase(newText, null) // checkId가 없으면 새로운 아이템으로 저장
            }
            alertDialogInstance.dismiss()
        }
    }

    // RealTime DB : CheckList 수정
    private fun updateItemText(newText: String, checkId: String) {
        val database = Firebase.database
        val itemRef = database.getReference("TravelList").child(userId).child(travelListId ?: "").child("check").child(checkId)

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
    private fun deleteItem(checkId: String, position: Int) {
        val database = Firebase.database
        val itemRef = database.getReference("TravelList").child(userId).child(travelListId?: "").child("check").child(checkId)

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
        val itemRef = database.getReference("TravelList").child(userId).child(travelListId?: "").child("check").child(checkId)

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
        if (position >= 0 && position < dataList.size) {
            dataList.removeAt(position)
            notifyItemRemoved(position) // 이 부분이 삭제된 항목을 RecyclerView에 알리는 부분
            notifyItemRangeChanged(position, itemCount) // 삭제된 항목 이후의 항목들의 위치를 변경 후  RecyclerView에 알리는 부분
        }
    }


    // 데이터 추가 메서드
    // 데이터 추가 메서드
    fun addData(newItem: Check) {
        dataList.add(newItem)
        notifyItemInserted(dataList.size - 1) // 마지막 항목에 추가된 것을 알리고 RecyclerView를 업데이트
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