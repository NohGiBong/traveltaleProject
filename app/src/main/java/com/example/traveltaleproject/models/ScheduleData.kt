package com.example.traveltaleproject.models

data class ScheduleData(
    var startTime: Long,
    var endTime: Long,
    var scheduleText: String?,
    val scheduleTimeId: String
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "startTime" to startTime,
            "endTime" to endTime,
            "scheduleText" to scheduleText, // Nullable한 String을 그대로 사용
            "scheduleTimeId" to scheduleTimeId
        )
    }

}