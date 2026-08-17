package com.uvaustralia.app.domain

import kotlin.math.*

data class Station(
    val code: String,
    val displayName: String,
    val lat: Double,
    val lon: Double,
    val isAustralian: Boolean = true,
)

val AUSTRALIAN_STATIONS = listOf(
    Station("adl", "Adelaide",       -34.9524, 138.5196),
    Station("ali", "Alice Springs",  -23.7622, 133.8739),
    Station("bri", "Brisbane",       -27.4698, 153.0251),
    Station("can", "Canberra",       -35.3222, 149.0000),
    Station("dar", "Darwin",         -12.4634, 130.8456),
    Station("emd", "Emerald",        -23.5274, 148.1668),
    Station("gco", "Gold Coast",     -28.1674, 153.5172),
    Station("kin", "Kingston",       -42.9818, 147.3050),
    Station("mel", "Melbourne",      -37.8136, 144.9631),
    Station("new", "Newcastle",      -32.9283, 151.7817),
    Station("per", "Perth",          -31.9505, 115.8605),
    Station("syd", "Sydney",         -33.8688, 151.2093),
    Station("tow", "Townsville",     -19.2590, 146.7785),
)

val ELSEWHERE_STATIONS = listOf(
    Station("cas", "Casey",           -66.2812, 110.5000, isAustralian = false),
    Station("dav", "Davis",           -68.5763,  77.9671, isAustralian = false),
    Station("maw", "Mawson",          -67.6000,  62.8851, isAustralian = false),
    Station("mcq", "Macquarie Island",-54.4900, 158.8425, isAustralian = false),
)

val ALL_STATIONS = AUSTRALIAN_STATIONS + ELSEWHERE_STATIONS

fun Station.distanceTo(lat: Double, lon: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat - this.lat)
    val dLon = Math.toRadians(lon - this.lon)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(this.lat)) * cos(Math.toRadians(lat)) * sin(dLon / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

fun nearestStation(lat: Double, lon: Double): Station =
    ALL_STATIONS.minByOrNull { it.distanceTo(lat, lon) } ?: ALL_STATIONS.first()
