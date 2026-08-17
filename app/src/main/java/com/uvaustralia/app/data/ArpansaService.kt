package com.uvaustralia.app.data

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ArpansaService {
    @GET("xml/uvvalues.xml")
    suspend fun getLiveReadings(): String

    @GET("api/uvlevel/")
    suspend fun getCurveData(
        @Query("longitude") longitude: Double,
        @Query("latitude") latitude: Double,
        @Query("date") date: String,
    ): String
}

object ArpansaRetrofit {
    val service: ArpansaService by lazy {
        Retrofit.Builder()
            .baseUrl("https://uvdata.arpansa.gov.au/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(ArpansaService::class.java)
    }
}
