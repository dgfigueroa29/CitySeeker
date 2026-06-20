package com.boa.test.city.seeker.data.source

import android.content.Context
import com.boa.test.city.seeker.R
import com.boa.test.city.seeker.data.network.CityApi
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class CityDownloader(
    private val cityApi: CityApi,
    private val context: Context,
    private val fileProcessor: CityFileProcessor,
) {
    suspend fun downloadAndProcess(tempFile: File) {
        val response = cityApi.getAllCities()
        if (response.isSuccessful) {
            response.body()?.let { body ->
                body.use {
                    try {
                        it.byteStream().use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        fileProcessor.processFile(tempFile)
                    } catch (e: Exception) {
                        Timber.e("Error processing downloaded cities: ${e.stackTraceToString()}")
                    }
                }
            }
        } else {
            fallbackToRawResource(tempFile)
        }
    }

    private suspend fun fallbackToRawResource(tempFile: File) {
        try {
            val inputStream = context.resources.openRawResource(R.raw.cities)
            inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            fileProcessor.processFile(tempFile)
        } catch (e: Exception) {
            Timber.e("Error loading cities from raw resource: ${e.stackTraceToString()}")
        }
    }
}
