package com.uvaustralia.app.domain

enum class UviBand(val displayName: String, val lowerBound: Double) {
    LOW("Low", 0.0),
    MODERATE("Moderate", 3.0),
    HIGH("High", 6.0),
    VERY_HIGH("Very High", 8.0),
    EXTREME("Extreme", 11.0),
}

fun uviBandFor(uv: Double): UviBand = when {
    uv < 3.0  -> UviBand.LOW
    uv < 6.0  -> UviBand.MODERATE
    uv < 8.0  -> UviBand.HIGH
    uv < 11.0 -> UviBand.VERY_HIGH
    else      -> UviBand.EXTREME
}
