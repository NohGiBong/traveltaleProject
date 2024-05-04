//package com.example.traveltaleproject
//
//import android.graphics.Typeface
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.RelativeLayout
//import android.widget.TextView
//import androidx.cardview.widget.CardView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//
//class ScheduleItemAdapter(private var scheduleList: MutableList<String>): RecyclerView.Adapter<ScheduleItemAdapter.MyViewHolder>() {
//
//    // 항목 구성에 필요한 뷰 홀더 객체 준비
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_schedule_item, parent, false)
//        return MyViewHolder(view)
//    }
//
//    // 뷰에 데이터 출력
//    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        holder.bind(scheduleList[position])
//
//        if(position % 4 == 0) {
//            holder.setFirstStyle()
//        } else if(position % 4 == 1) {
//            holder.setSecondStyle()
//        } else if(position % 4 == 2) {
//            holder.setThirdStyle()
//        } else {
//            holder.setForthStyle()
//        }
//    }
//
//    // 항목의 개수 구하기
//    override fun getItemCount(): Int = scheduleList.size
//
//    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val view: RelativeLayout = itemView.findViewById<RelativeLayout>(R.id.schedule_item_edit)
//        val cardView: CardView = view.findViewById<CardView>(R.id.schedule_txt_edit)
//        val timeTxt: TextView = view.findViewById(R.id.time_txt)
//
//        fun bind(text: String) {
//            timeTxt.text = text
//        }
//
//        fun setFirstStyle() {
//            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.first))
//        }
//
//        fun setSecondStyle() {
//            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.second))
//        }
//
//        fun setThirdStyle() {
//            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.third))
//        }
//
//        fun setForthStyle() {
//            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.forth))
//        }
//    }
//
//    // 데이터 업데이트 메서드
//    fun updateSchedule(newScheduleList: MutableList<String>) {
//        scheduleList.clear()
//        scheduleList.addAll(newScheduleList)
//        notifyDataSetChanged()
//    }
//}
//
//
