package com.example.traveltaleproject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.databinding.ActivityScheduleDayItemBinding

class MyViewHolder(val binding: ActivityScheduleDayItemBinding): RecyclerView.ViewHolder(binding.root)
class ScheduleDayItemAdapter(val scheduleList: MutableList<String>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedPosition = 0
    override fun getItemCount(): Int = scheduleList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MyViewHolder(ActivityScheduleDayItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MyViewHolder).binding

        // 뷰에 데이터 출력
        binding.scheduleDayTxt.text = scheduleList[position]

        // 배경색상 설정
        if (position == selectedPosition) {
            // 첫 번째 아이템일 경우 배경색상 변경
            binding.dayItem.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.selected))
            binding.scheduleDayTxt.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        } else {
            // 그 외의 경우 기본 배경색상 유지
            binding.dayItem.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.unselected))
            binding.scheduleDayTxt.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
        }

        // 뷰에 이벤트 추가
        binding.dayItem.setOnClickListener {
            // 클릭한 아이템 위치 업데이트
            selectedPosition = holder.adapterPosition
            notifyDataSetChanged()
        }
    }


}