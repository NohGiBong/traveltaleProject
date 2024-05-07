package com.example.traveltaleproject.travellist

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.GetActivity
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityTravellistItemBinding
import com.example.traveltaleproject.models.Check
import com.example.traveltaleproject.models.TravelList

class TravelListAdapter(private val context: Context, private val travelList: MutableList<TravelList>, private val userId: String) : RecyclerView.Adapter<TravelListAdapter.TravelListViewHolder>() {

    inner class TravelListViewHolder(private val binding: ActivityTravellistItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }
        fun bind(travel: TravelList) {
            binding.apply {
                itemTitle.text = travel.title
                startDateTxt.text = travel.startDate
                endDateTxt.text = travel.endDate
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
    fun setList(newDataList: MutableList<TravelList>) {
        travelList.clear()
        for (item in newDataList) {
            // 등록된 순서대로 데이터를 추가
            travelList.add(0, item)
        }
        notifyDataSetChanged()
    }
}
