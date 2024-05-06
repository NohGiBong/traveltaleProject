package com.example.traveltaleproject.schedule

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.R

class ScheduleDayToDayAdapter(private var scheduleDayList: MutableList<String>) : RecyclerView.Adapter<ScheduleDayToDayAdapter.MyViewHolder>() {

//    private var selectedPosition = 0
//    private var fragmentManager: FragmentManager? = null

    private var selectedPosition: String? = null
    private var fragmentManager: FragmentManager? = null

    override fun getItemCount(): Int = scheduleDayList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_schedule_day_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(scheduleDayList[position])

        if (scheduleDayList[position] == selectedPosition) {
            holder.setSelectedStyle()
        } else {
            holder.setDefaultStyle()
        }

        holder.itemView.setOnClickListener {
            selectedPosition = scheduleDayList[position]
            notifyDataSetChanged()
            // 아이템 클릭 이벤트를 액티비티로 전달
            showFragmentForDate(selectedPosition)
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

    private fun showFragmentForDate(date: String?) {
        fragmentManager?.let { manager ->
            // 이미 해당 태그를 가진 프래그먼트가 있는지 확인
            val existingFragment = manager.findFragmentByTag(date)
            if (existingFragment != null) {
                // 이미 추가된 경우 아무 작업도 하지 않고 종료
                return
            }

            val transaction = manager.beginTransaction()
            // ScheduleFragment.newInstance 메서드로 새 프래그먼트를 생성하고 추가합니다.
            val newFragment = ScheduleFragment.newInstance(date.toString())
            transaction.replace(R.id.fragment_view, newFragment, date)
            transaction.commit()

            // 클릭한 날짜에 해당하는 프래그먼트를 보이도록 설정
            val fragmentLayout = (manager.findFragmentById(R.id.fragment_view)?.view?.parent as? ViewGroup)
            fragmentLayout?.visibility = View.VISIBLE
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
