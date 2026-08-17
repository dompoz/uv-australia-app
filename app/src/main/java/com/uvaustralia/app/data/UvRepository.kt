package com.uvaustralia.app.data

import com.uvaustralia.app.domain.UvCurvePoint
import com.uvaustralia.app.domain.UvReading
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class UvRepository {
    private val service = ArpansaRetrofit.service

    fun liveReadingsFlow(pollIntervalMs: Long = 60_000L): Flow<Result<List<UvReading>>> = flow {
        while (true) {
            val result = runCatching {
                val xml = service.getLiveReadings()
                ArpansaXmlParser.parseXml(xml)
            }
            emit(result)
            delay(pollIntervalMs)
        }
    }

    suspend fun fetchCurve(lat: Double, lon: Double, date: LocalDate): Result<List<UvCurvePoint>> =
        runCatching {
            val dateStr = ArpansaCurveParser.curveApiDate(date)
            val json = service.getCurveData(longitude = lon, latitude = lat, date = dateStr)
            ArpansaCurveParser.parseCurve(json)
        }
}
