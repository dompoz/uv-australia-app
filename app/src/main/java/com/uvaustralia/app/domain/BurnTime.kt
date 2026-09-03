package com.uvaustralia.app.domain

private const val K_TYPE_I  = 2.5
private const val K_TYPE_V  = 8.0

fun burnTimeMinutes(uvi: Double, k: Double): Double = (200.0 * k) / (3.0 * uvi)

fun burnEasilyMinutes(uvi: Double): Double? {
    if (uvi < 0.5) return null
    return burnTimeMinutes(uvi, K_TYPE_I)
}

fun burnRarelyMinutes(uvi: Double): Double? {
    if (uvi < 0.5) return null
    return burnTimeMinutes(uvi, K_TYPE_V)
}

fun formatBurnTime(minutes: Double): String {
    return if (minutes < 60.0) {
        "${minutes.toInt()} min"
    } else {
        val hours = minutes / 60.0
        "${"%.1f".format(hours)} hrs"
    }
}
