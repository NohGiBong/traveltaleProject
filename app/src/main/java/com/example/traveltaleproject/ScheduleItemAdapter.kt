import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traveltaleproject.ChecklistViewHolder
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityChecklistItemBinding
import com.example.traveltaleproject.schedule.Schedule

class ScheduleItemAdapter(val scheduleList: MutableList<Schedule>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return scheduleList?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ChecklistViewHolder(
            ActivityChecklistItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ChecklistViewHolder).binding

        binding.chkTxtEdit.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                // Enter 키가 눌렸을 때 새로운 아이템을 추가할 데이터 준비
                val newItemText = binding.chkTxtEdit.text.toString()

                // RecyclerView 어댑터에 데이터가 변경되었음을 알림
                notifyDataSetChanged()

                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }

}
