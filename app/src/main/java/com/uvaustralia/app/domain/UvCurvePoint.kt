package com.uvaustralia.app.domain

data class UvCurvePoint(
    val minutesFromMidnight: Int,
    val forecast: Double?,
    val measured: Double?,
)
