package com.boa.test.city.seeker.data.source

import android.content.Context
import com.boa.test.city.seeker.common.FILE_CITY
import java.io.File

class CityCacheManager(
    private val context: Context,
) {
    data class CacheFileResult(
        val file: File,
        val needsDownload: Boolean,
    )

    fun resolveCacheFile(): CacheFileResult {
        val cacheDir = context.cacheDir
        val cacheFile = File(cacheDir, FILE_CITY)
        val isValid = cacheFile.exists() && cacheFile.length() > 0L && !cacheFile.isDirectory

        return if (isValid) {
            CacheFileResult(cacheFile, needsDownload = false)
        } else {
            val tempFile = File(cacheDir, "temp_$FILE_CITY")
            if (!tempFile.exists()) {
                tempFile.createNewFile()
            }
            CacheFileResult(tempFile, needsDownload = true)
        }
    }

    fun finalizeCacheFile(tempFile: File) {
        val cacheDir = context.cacheDir
        val cacheFile = File(cacheDir, FILE_CITY)
        if (tempFile.exists() && tempFile.name.startsWith("temp_")) {
            tempFile.renameTo(cacheFile)
        }
    }
}
