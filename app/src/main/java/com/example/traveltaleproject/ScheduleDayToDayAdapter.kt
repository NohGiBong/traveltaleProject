package com.example.traveltaleproject

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.R


class ScheduleDayToDayAdapter(private var scheduleDayList: MutableList<String>) : RecyclerView.Adapter<ScheduleDayToDayAdapter.MyViewHolder>() {

    private var selectedPosition = 0
    private var fragmentManager: FragmentManager? = null

    override fun getItemCount(): Int = scheduleDayList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_schedule_day_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(scheduleDayList[position])

        if (position == selectedPosition) {
            holder.setSelectedStyle()
        } else {
            holder.setDefaultStyle()
        }

        holder.itemView.setOnClickListener {
            selectedPosition = holder.adapterPosition
            notifyDataSetChanged()
            // 클릭된 날짜에 해당하는 프래그먼트를 표시
            val selectedDate = scheduleDayList[selectedPosition]
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayItem: CardView = itemView.findViewById(R.id.day_item)
        private val scheduleDayTxt: TextView = itemView.findViewById(R.id.schedule_day_txt)

        fun bind(text: String) {
            scheduleDayTxt.text = text
        }

        fun setSelectedStyle() {
            dayItem.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.selected))
            scheduleDayTxt.apply {
                setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                setTypeface(null, Typeface.BOLD)
            }
        }

        fun setDefaultStyle() {
            dayItem.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.unselected))
            scheduleDayTxt.apply {
                setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    private fun showFragmentForDate(date: String) {
        fragmentManager?.let { manager ->
            val transaction = manager.beginTransaction()
            // ScheduleFragment.newInstance 메서드로 새 프래그먼트를 생성하고 추가합니다.
            val newFragment = ScheduleFragment.newInstance(date)
            transaction.replace(R.id.fragment_view, newFragment)
            transaction.commit()
        }
    }

    fun setFragmentManager(manager: FragmentManager) {
        this.fragmentManager = manager
    }

    fun updateSchedule(newScheduleList: MutableList<String>) {
        scheduleDayList = newScheduleList
        notifyDataSetChanged()
    }
}




