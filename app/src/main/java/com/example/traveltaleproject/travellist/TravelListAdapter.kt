package com.example.traveltaleproject.travellist

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.GetActivity
import com.example.traveltaleproject.databinding.ActivityTravellistItemBinding
import com.example.traveltaleproject.models.TravelList
import com.example.traveltaleproject.utils.SwipeToDeleteCallbackTravelList
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
            }
        }

        override fun onClick(view: View?) {
            // 현재 아이템의 TravelListId를 가져옴
            val itemId = travelList[adapterPosition].travelListId

            // GetActivity로 이동하기 위한 Intent 생성
            val intent = Intent(context, GetActivity::class.java).apply {
                // TravelListId를 Intent에 추가하여 전달
                putExtra("travelListId", itemId)
            }

            // Intent를 이용하여 GetActivity 시작
            context.startActivity(intent)
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

    fun deleteItem(position: Int) {
        val deletedItem = travelList[position]

        // Realtime Database에서 해당 아이템 삭제
        val database = FirebaseDatabase.getInstance().getReference("TravelList").child(userId).child(deletedItem.travelListId)
        database.removeValue()

        // RecyclerView에서 아이템 삭제
        travelList.removeAt(position)
        notifyItemRemoved(position)
    }
    fun setList(newDataList: MutableList<TravelList>) {
        travelList.clear()
        for (item in newDataList) {
            // 등록된 순서대로 데이터를 추가
            travelList.add(0, item)
        }
        notifyDataSetChanged()
    }
}
