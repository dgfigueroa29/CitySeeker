package com.boa.test.city.seeker.common.map

import android.content.Context
import com.mapbox.common.TileStore
import timber.log.Timber

object OfflineTileManager {
    private const val TAG = "OfflineTiles"

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        val path = context.getDir("map_tiles", Context.MODE_PRIVATE).absolutePath
        TileStore.setRootPath(path)
        TileStore.create()
        isInitialized = true
        Timber.tag(TAG).d("TileStore initialized: %s", path)
    }
}
