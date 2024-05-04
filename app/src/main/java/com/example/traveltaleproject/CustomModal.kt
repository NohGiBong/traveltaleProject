import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.traveltaleproject.R
import com.example.traveltaleproject.databinding.ActivityScheduleModalBinding

class CustomModal(context: Context): Dialog(context) {
    private lateinit var binding: ActivityScheduleModalBinding
    data class ScheduleData(
        val startTime: Long?,
        val endTime: Long?,
        val scheduleText: String?
    )
    private var saveButtonClickListener: SaveButtonClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleModalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // 외부 클릭으로 다이얼로그 닫기 허용
        setCanceledOnTouchOutside(true)
        // 취소 가능 여부 설정
        setCancelable(true)

        // 뷰 요소 초기화
        val startTimeSpinner = binding.startTimeSpinner
        val endTimeSpinner = binding.endTimeSpinner
        val scheduleTxtEdit = binding.modalScheduleEdit
        val submitBtn = binding.scheduleAdd

        // 선택된 시간 변수 초기화
        var selectedStartTime: Long? = null
        var selectedEndTime: Long? = null

        // 시작 시간 스피너 이벤트 처리
        startTimeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedString = parent?.getItemAtPosition(position) as? String
                selectedStartTime = if (selectedString != null && selectedString.length >= 2) {
                    try {
                        selectedString.substring(0, 2).toLong()
                    } catch (e: NumberFormatException) {
                        // 변환 중 오류 발생 시, 기본값이나 null 반환
                        null
                    }
                } else {
                    null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 종료 시간 스피너 이벤트 처리
        endTimeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedString = parent?.getItemAtPosition(position) as? String
                selectedEndTime = if (selectedString != null && selectedString.length >= 2) {
                    try {
                        selectedString.substring(0, 2).toLong()
                    } catch (e: NumberFormatException) {
                        // 변환 중 오류 발생 시, 기본값이나 null 반환
                        null
                    }
                } else {
                    null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 저장 버튼 클릭 이벤트 처리
        submitBtn.setOnClickListener {
            val scheduleText = scheduleTxtEdit.text.toString()

            if (scheduleText.isNotBlank()) {
                // 입력된 스케줄이 있는지 확인
                val scheduleData = ScheduleData(selectedStartTime, selectedEndTime, scheduleText)
                saveButtonClickListener?.onSaveButtonClicked(scheduleData)

                val scheduleInfo = "Start Time: $selectedStartTime, End Time: $selectedEndTime, Schedule: $scheduleText"
                Toast.makeText(context, scheduleInfo, Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                // 값이 누락되었을 때 에러 표시
                val modalScheduleTxtBox = binding.modalScheduleEditCard
//                modalScheduleTxtBox.setCardBackgroundColor(context.getColor(R.color.error))
            }
        }
    }

    // 저장 버튼 클릭 리스너 인터페이스
    interface SaveButtonClickListener {
        fun onSaveButtonClicked(scheduleData: ScheduleData)
    }

    // 저장 버튼 클릭 리스너 설정 메서드
    fun setOnSaveButtonClickListener(listener: SaveButtonClickListener) {
        saveButtonClickListener = listener
    }
}
