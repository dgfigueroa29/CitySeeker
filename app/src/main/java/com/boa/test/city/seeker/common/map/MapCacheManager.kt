package com.boa.test.city.seeker.common.map

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import timber.log.Timber

object MapCacheManager {
    private const val TAG = "MapCache"

    fun init(context: Context) {
        Timber.tag(TAG).d("Map cache path: %s/map_cache", context.filesDir)
    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(network)
        return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
