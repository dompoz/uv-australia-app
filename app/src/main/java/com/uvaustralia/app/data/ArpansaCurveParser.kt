package com.uvaustralia.app.data

import com.uvaustralia.app.domain.UvCurvePoint
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ArpansaCurveParser {

    fun curveApiDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    fun parseCurve(json: String): List<UvCurvePoint> {
        val result = mutableListOf<UvCurvePoint>()
        runCatching {
            val root = JSONObject(json)
            val arr: JSONArray = root.getJSONArray("GraphData")
            for (i in 0 until arr.length()) {
                val obj: JSONObject = arr.getJSONObject(i)
                val timestamp = obj.optString("Date", "")
                val forecast = obj.optDouble("Forecast", Double.NaN).takeUnless { it.isNaN() }
                val measured = obj.optDouble("Measured", Double.NaN).takeUnless { it.isNaN() }
                val minutes = parseTimestampToMinutes(timestamp)
                if (minutes >= 0) {
                    result += UvCurvePoint(minutes, forecast, measured)
                }
            }
        }
        return result
    }

    private fun parseTimestampToMinutes(timestamp: String): Int {
        // Expected format: "2025-02-02 06:00"
        return runCatching {
            val timePart = timestamp.trim().substringAfterLast(" ")
            val parts = timePart.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        }.getOrDefault(-1)
    }
}
