package com.uvaustralia.app.domain

const val PROTECTION_THRESHOLD = 3.0
const val GRAPH_START_HOUR = 6   // 6 AM
const val GRAPH_END_HOUR = 20    // 8 PM
const val GRAPH_MAX_UV = 16.0

data class ProtectionWindow(
    val startMinutes: Int,
    val endMinutes: Int,
) {
    fun startDisplay(): String = formatMinutes(startMinutes)
    fun endDisplay(): String = formatMinutes(endMinutes)
}

private fun formatMinutes(totalMinutes: Int): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    val ampm = if (h < 12) "AM" else "PM"
    val h12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return if (m == 0) "$h12 $ampm" else "$h12:%02d $ampm".format(m)
}

fun computeProtectionWindow(curve: List<UvCurvePoint>): ProtectionWindow? {
    val above = curve.filter { (it.forecast ?: 0.0) >= PROTECTION_THRESHOLD }
    if (above.isEmpty()) return null

    val rawStart = above.first().minutesFromMidnight
    val rawEnd = above.last().minutesFromMidnight

    // Round start DOWN to nearest 15 min, end UP to nearest 15 min
    val start = (rawStart / 15) * 15
    val end = ((rawEnd + 14) / 15) * 15

    return ProtectionWindow(start, end)
}
