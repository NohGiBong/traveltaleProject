package com.example.traveltaleproject.schedule

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityScheduleDayItemBinding



class ScheduleDayToDayAdapter(
    private var scheduleDayList: MutableList<String>
) : RecyclerView.Adapter<ScheduleDayToDayAdapter.MyViewHolder>() {
    private var selectedPosition: String? = null
    interface OnItemClickListener {
        fun onItemClick(date: String)
    }

    // 클릭 리스너 변수 선언
    private var itemClickListener: OnItemClickListener? = null

    // 외부에서 클릭 리스너 설정 메서드
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    inner class MyViewHolder(private val binding: ActivityScheduleDayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val date = scheduleDayList[position]
                    itemClickListener?.onItemClick(date)
                }
            }
        }

        fun bind(text: String) {
            binding.scheduleDayTxt.text = text
        }

        fun setSelectedStyle() {
            binding.dayItem.setCardBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.selected
                )
            )
            binding.scheduleDayTxt.apply {
                setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                setTypeface(null, Typeface.BOLD)
            }
        }

        fun setDefaultStyle() {
            binding.dayItem.setCardBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.unselected
                )
            )
            binding.scheduleDayTxt.apply {
                setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                setTypeface(null, Typeface.NORMAL)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ActivityScheduleDayItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = scheduleDayList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(scheduleDayList[position])

        if (scheduleDayList[position] == selectedPosition) {
            holder.setSelectedStyle()
        } else {
            holder.setDefaultStyle()
        }

        // 아이템을 클릭했을 때
        holder.itemView.setOnClickListener {
            // 클릭한 아이템의 날짜를 가져와서 리스너에 전달합니다.
            val date = scheduleDayList[position]
            itemClickListener?.onItemClick(date)

            // 선택된 포지션을 업데이트합니다.
            selectedPosition = date

            // 변경된 포지션의 스타일을 업데이트합니다.
            notifyDataSetChanged()
        }
    }
}



