package com.uvaustralia.app.domain

data class UvReading(
    val stationCode: String,
    val index: Double,
    val time: String,
    val date: String,
    val status: String,
)
