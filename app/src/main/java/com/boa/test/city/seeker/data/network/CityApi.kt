package com.boa.test.city.seeker.data.network

import com.boa.test.city.seeker.common.FILE_CITY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming

interface CityApi {
    @Streaming
    @GET(FILE_CITY)
    suspend fun getAllCities(): Response<ResponseBody>
}
