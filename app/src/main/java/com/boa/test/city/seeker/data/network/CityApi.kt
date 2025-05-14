package com.boa.test.city.seeker.data.network

import com.boa.test.city.seeker.common.FILE_CITY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming

/**
 * Retrofit interface for accessing city data from a remote source.
 */
interface CityApi {
    /**
     * Retrieves all city data from the remote source.
     *
     * @return A [Response] containing the raw [ResponseBody] of the city data.
     */
    @Streaming
    @GET(FILE_CITY)
    suspend fun getAllCities(): Response<ResponseBody>
}
