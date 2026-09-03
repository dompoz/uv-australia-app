package com.uvaustralia.app.domain

data class UvBandInfo(
    val riskLabel: String,
    val riskSummary: String,
    val showProtectionSteps: Boolean,
)

val SUNSMART_STEPS: List<Pair<String, String>> = listOf(
    "Slip" to "on sun-protective clothing covering your arms and legs.",
    "Slop" to "on SPF 50+ broad-spectrum, water-resistant sunscreen. Reapply every 2 hours.",
    "Slap" to "on a broad hat with a brim at least 7.5 cm to protect your face and neck.",
    "Seek" to "shade, especially during peak hours (10 am – 4 pm).",
    "Slide" to "on sunglasses that meet Australian standards.",
)

fun uvBandInfoFor(band: UviBand, uvIndex: Double = band.lowerBound): UvBandInfo {
    if (band == UviBand.LOW && uvIndex < 0.3) {
        return UvBandInfo(
            riskLabel = "No UV",
            riskSummary = "No UV radiation is currently detected. No sun protection is needed.",
            showProtectionSteps = false,
        )
    }
    return when (band) {
        UviBand.LOW -> UvBandInfo(
            riskLabel = "Low risk",
            riskSummary = "UV levels are low. Short outdoor activities carry minimal risk, though all UV exposure still adds up over your lifetime.",
            showProtectionSteps = false,
        )
        UviBand.MODERATE -> UvBandInfo(
            riskLabel = "Moderate risk",
            riskSummary = "UV is strong enough to damage unprotected skin. Protection is recommended whenever you're outdoors.",
            showProtectionSteps = true,
        )
        UviBand.HIGH -> UvBandInfo(
            riskLabel = "High risk",
            riskSummary = "UV levels are high. Unprotected skin can be damaged quickly. Reduce time outdoors and use full protection.",
            showProtectionSteps = true,
        )
        UviBand.VERY_HIGH -> UvBandInfo(
            riskLabel = "Very high risk",
            riskSummary = "UV is very high. Full protection is essential. Seek shade and limit direct sun exposure wherever possible.",
            showProtectionSteps = true,
        )
        UviBand.EXTREME -> UvBandInfo(
            riskLabel = "Extreme risk",
            riskSummary = "Extreme UV levels. Avoid being outdoors if possible. Maximum protection measures apply at all times.",
            showProtectionSteps = true,
        )
    }
}
