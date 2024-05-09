package com.example.traveltaleproject.travellist

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.GetActivity
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityTravellistItemBinding
import com.example.traveltaleproject.models.TravelList
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class TravelListAdapter(private val context: Context, private val travelList: MutableList<TravelList>, private val userId: String) : RecyclerView.Adapter<TravelListAdapter.TravelListViewHolder>() {

    inner class TravelListViewHolder(private val binding: ActivityTravellistItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }
        fun bind(travel: TravelList) {
            binding.apply {
                itemTitle.text = travel.title
                val startDate = SimpleDateFormat("dd. MMM. yyyy", Locale.ENGLISH).format(travel.startDate)
                val endDate = SimpleDateFormat("dd. MMM. yyyy", Locale.ENGLISH).format(travel.endDate)
                startDateTxt.text = startDate
                endDateTxt.text = endDate

                itemDel.setOnClickListener{
                    showDeleteDialog()
                }
            }
        }

        override fun onClick(view: View?) {
            // 현재 아이템의 TravelListId를 가져옴
            val itemId = travelList[bindingAdapterPosition].travelListId

            // GetActivity로 이동하기 위한 Intent 생성
            val intent = Intent(context, GetActivity::class.java).apply {
                // TravelListId를 Intent에 추가하여 전달
                putExtra("travelListId", itemId)
            }

            // Intent를 이용하여 GetActivity 시작
            context.startActivity(intent)
        }

        private fun showDeleteDialog() {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("정말로 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ -> deleteItem(bindingAdapterPosition) }
                .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }

            val dialog = builder.create()

            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(context, R.color.black))
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(context, R.color.black))
            }

            dialog.show()
        }

        private fun deleteItem(position: Int) {
            if (position < 0 || position >= travelList.size) {
                Toast.makeText(context, "Invalid position", Toast.LENGTH_SHORT).show()
                Log.e("TravelListAdapter", "Invalid position: $position")
                return
            }
            val travel = travelList[position]

            // DB에서 아이템 삭제 기능
            FirebaseDatabase.getInstance().reference.child("TravelList")
                .child(userId)
                .child(travel.travelListId)
                .removeValue()
                .addOnSuccessListener {
                    // 삭제 성공 시 토스트
                    Toast.makeText(context, "트레블 삭제 완료", Toast.LENGTH_SHORT).show()

                    // 아이템 위치 유효성 확인
                    if (position >= travelList.size) {
                        Log.e("TravelListAdapter", "Position became invalid after deletion: $position, List size: ${travelList.size}")
                        return@addOnSuccessListener
                    }

                    // 아이템 삭제 후 어댑터 갱신
                    travelList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, itemCount)
                }
                .addOnFailureListener { e ->
                    // 삭제 실패 시 토스트
                    Toast.makeText(context, "트레블 삭제 살패${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelListViewHolder {
        val binding = ActivityTravellistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TravelListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TravelListViewHolder, position: Int) {
        val currentItem = travelList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return travelList.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setList(newDataList: MutableList<TravelList>) {
        travelList.clear()
        travelList.addAll(newDataList)
        notifyDataSetChanged()
    }
}
