package com.example.traveltaleproject.schedule

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.R
import com.example.traveltaleproject.models.Check
import com.example.traveltaleproject.models.ScheduleData
import com.google.firebase.database.FirebaseDatabase


class ScheduleItemAdapter(private val context: Context?,
                          private var dataSet: MutableList<ScheduleData>,
                          private val userId: String,
                          private val travelListId: String,
                          private val daySection: String) :
    RecyclerView.Adapter<ScheduleItemAdapter.ScheduleViewHolder>() {

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.activity_schedule_item, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val item = dataSet[position]

        // 시간 설정
        val startTime = item.startTime
        val endTime = item.endTime
        val formattedStartTime = if (startTime < 10) "0$startTime AM" else "$startTime AM"
        val formattedEndTime = if (endTime < 10) "0$endTime AM" else "$endTime AM"
        holder.startTimeText.text = formattedStartTime
        holder.endTimeText.text = formattedEndTime

        // 카드뷰의 높이를 동적으로 설정
        // 시간 간격에 따라 높이 계산
        val heightInPixels = calculateHeightForItem(startTime.toInt(), endTime.toInt())
        val layoutParams2 = holder.timeText.layoutParams
        val layoutParams = holder.cardView.layoutParams
        layoutParams2.height = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams.height = heightInPixels
        holder.timeText.layoutParams = layoutParams
        holder.cardView.layoutParams = layoutParams


        context?.let { ctx ->
            // 높이에 따라 색상 변경
            if (heightInPixels % 4 == 1) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context,
                    R.color.first
                ))
            } else if (heightInPixels % 4 == 2) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context,
                    R.color.second
                ))
            } else if (heightInPixels % 4 == 3) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context,
                    R.color.third
                ))
            } else if (heightInPixels % 4 == 0) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context,
                    R.color.forth
                ))
            }
        }

        // schedule_text
        holder.scheduleText.text = "${item.scheduleText}"

        holder.editBtn.setOnClickListener {
            showEditModal(item)
        }

        holder.deleteBtn.setOnClickListener {
            deleteItemFromDatabase(item)
            dataSet.remove(item)
            notifyDataSetChanged()
        }
    }

    private fun calculateHeightForItem(startTime: Int, endTime: Int): Int {
        // 시간 간격에 따라 높이 계산
        val timeSlotHeightDp = 50 // 1시간 당 높이 (dp)
        val timeSlotHeightPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            timeSlotHeightDp.toFloat(),
            Resources.getSystem().displayMetrics
        )
        val duration = endTime - startTime
        return (timeSlotHeightPixels * duration).toInt()
    }

    private fun showEditModal(item: ScheduleData) {
        context?.let { ctx ->
            val dialog = CustomModal(ctx, daySection, travelListId, userId)
            dialog.setScheduleData(item) // 해당 아이템의 정보를 모달창에 전달
            dialog.show()
        }
    }

    private fun deleteItemFromDatabase(item: ScheduleData) {
        val databaseReference = FirebaseDatabase.getInstance().reference
            .child("TravelList").child(userId).child(travelListId)
            .child("schedule").child(daySection).child(item.scheduleTimeId)

        databaseReference.removeValue()
            .addOnSuccessListener {
                Log.d("FirebaseDB", "아이템 삭제 성공")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseDB", "아이템 삭제 실패", e)
            }
    }

    inner class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val startTimeText: TextView by lazy { view.findViewById<TextView>(R.id.schedule_start_time) }
        val endTimeText: TextView by lazy { view.findViewById<TextView>(R.id.schedule_end_time) }
        val cardView: CardView by lazy { view.findViewById<CardView>(R.id.schedule_txt_cardview) }
        val timeText: RelativeLayout by lazy { view.findViewById<RelativeLayout>(R.id.time_txt) }
        val scheduleText: TextView by lazy { view.findViewById<TextView>(R.id.schedule_txt) }
        val editBtn: ImageButton by lazy { view.findViewById<ImageButton>(R.id.edit_btn) }
        val deleteBtn: ImageButton by lazy { view.findViewById<ImageButton>(R.id.delete_btn) }
    }

    fun setData(newDataList: MutableList<ScheduleData>) {
        dataSet.clear()
        dataSet.addAll(newDataList)
        notifyDataSetChanged()
    }

}