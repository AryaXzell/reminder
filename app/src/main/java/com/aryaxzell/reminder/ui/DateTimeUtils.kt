package com.aryaxzell.reminder.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    const val TIME_PRESET_MORNING = "Morning"
    const val TIME_PRESET_AFTERNOON = "Afternoon"
    const val TIME_PRESET_TONIGHT = "Tonight"

    val PRESET_HOURS = mapOf(
        TIME_PRESET_MORNING to Pair(9, 0),
        TIME_PRESET_AFTERNOON to Pair(13, 0),
        TIME_PRESET_TONIGHT to Pair(20, 0)
    )

    fun getPresetHourMinute(preset: String): Pair<Int, Int>? {
        return when {
            preset.startsWith(TIME_PRESET_MORNING, ignoreCase = true) -> Pair(9, 0)
            preset.startsWith(TIME_PRESET_AFTERNOON, ignoreCase = true) -> Pair(13, 0)
            preset.startsWith(TIME_PRESET_TONIGHT, ignoreCase = true) -> Pair(20, 0)
            else -> parseCustomTime(preset)
        }
    }

    fun parseCustomTime(timeStr: String): Pair<Int, Int>? {
        val parts = timeStr.trim().split(":")
        if (parts.size == 2) {
            val h = parts[0].toIntOrNull()
            val m = parts[1].toIntOrNull()
            if (h != null && m != null && h in 0..23 && m in 0..59) {
                return Pair(h, m)
            }
        }
        return null
    }

    fun formatCustomTime(hour: Int, minute: Int): String {
        return String.format(Locale.US, "%02d:%02d", hour, minute)
    }

    fun formatTimeDisplay(timeOfDay: String?): String {
        if (timeOfDay == null) return ""
        val (h, m) = getPresetHourMinute(timeOfDay) ?: return timeOfDay
        return when {
            timeOfDay.equals(TIME_PRESET_MORNING, ignoreCase = true) -> "Morning (09:00)"
            timeOfDay.equals(TIME_PRESET_AFTERNOON, ignoreCase = true) -> "Afternoon (13:00)"
            timeOfDay.equals(TIME_PRESET_TONIGHT, ignoreCase = true) -> "Tonight (20:00)"
            else -> formatCustomTime(h, m)
        }
    }

    fun combineDateAndTime(dateTimestamp: Long, timeOfDay: String?): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateTimestamp
        }
        if (timeOfDay != null) {
            val (h, m) = getPresetHourMinute(timeOfDay) ?: Pair(9, 0)
            cal.set(Calendar.HOUR_OF_DAY, h)
            cal.set(Calendar.MINUTE, m)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun combineDateWithHourMinute(dateTimestamp: Long, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateTimestamp
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getTodayWithTime(timeOfDay: String?): Long {
        val cal = Calendar.getInstance()
        val (h, m) = if (timeOfDay != null) {
            getPresetHourMinute(timeOfDay) ?: Pair(9, 0)
        } else {
            Pair(9, 0)
        }
        cal.set(Calendar.HOUR_OF_DAY, h)
        cal.set(Calendar.MINUTE, m)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun isDueToday(timestamp: Long?): Boolean {
        if (timestamp == null) return false
        val cal = Calendar.getInstance()
        val todayYear = cal.get(Calendar.YEAR)
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)

        cal.timeInMillis = timestamp
        return todayYear == cal.get(Calendar.YEAR) && todayDay == cal.get(Calendar.DAY_OF_YEAR)
    }

    fun normalizeToSection(timeOfDay: String?): String {
        if (timeOfDay == null) return TIME_PRESET_MORNING
        return when {
            timeOfDay.startsWith(TIME_PRESET_MORNING, ignoreCase = true) -> TIME_PRESET_MORNING
            timeOfDay.startsWith(TIME_PRESET_AFTERNOON, ignoreCase = true) -> TIME_PRESET_AFTERNOON
            timeOfDay.startsWith(TIME_PRESET_TONIGHT, ignoreCase = true) -> TIME_PRESET_TONIGHT
            else -> {
                val parsed = parseCustomTime(timeOfDay)
                if (parsed != null) {
                    when (parsed.first) {
                        in 0..11 -> TIME_PRESET_MORNING
                        in 12..17 -> TIME_PRESET_AFTERNOON
                        else -> TIME_PRESET_TONIGHT
                    }
                } else {
                    TIME_PRESET_MORNING
                }
            }
        }
    }
}
