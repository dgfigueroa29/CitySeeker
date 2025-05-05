package com.boa.test.city.seeker.presentation.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * ConnectivityReceiver is a BroadcastReceiver that listens for network connectivity changes.
 * It utilizes the ConnectivityManager to determine if the device is currently connected to a network
 * with internet access and notifies the provided callback accordingly.
 *
 * @property onNetworkChange A lambda function that is invoked whenever the network connectivity status changes.
 *                           It receives a Boolean indicating whether the device is connected to the internet (true) or not (false).
 */
class ConnectivityReceiver(private val onNetworkChange: (Boolean) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val isConnected =
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        onNetworkChange(isConnected)
    }
}