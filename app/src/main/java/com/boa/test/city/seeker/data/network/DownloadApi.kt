package com.boa.test.city.seeker.data.network

import com.boa.test.city.seeker.BuildConfig
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming

interface DownloadApi {
    @Streaming
    @GET(BuildConfig.CITIES_URL)
    suspend fun downloadCities(): Response<ResponseBody>
}
