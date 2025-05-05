package com.boa.test.city.seeker.presentation.component

import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.boa.test.city.seeker.presentation.sensor.ConnectivityReceiver

/**
 * Observes the network connectivity status and notifies the provided callback whenever the connection state changes.
 *
 * This composable uses a broadcast receiver to listen for changes in the network connectivity,
 * specifically for the `ConnectivityManager.CONNECTIVITY_ACTION` intent. It provides a boolean
 * value to the `onConnectionChanged` callback, indicating whether the device is currently connected
 * to a network (true) or not (false).
 *
 * The receiver is registered when the composable enters the composition and unregistered when it
 * leaves, ensuring proper resource management.
 *
 * @param onConnectionChanged A callback function that is invoked whenever the network connectivity
 *                            status changes. It receives a boolean parameter indicating whether a
 *                            network connection is available (true) or not (false).
 *
 * @see ConnectivityReceiver
 * @see ConnectivityManager.CONNECTIVITY_ACTION
 */
@Suppress("FunctionNaming")
@Composable
fun ConnectivityStatus(onConnectionChanged: (Boolean) -> Unit) {
    val context = LocalContext.current
    val connectivityReceiver = remember {
        ConnectivityReceiver(onConnectionChanged)
    }

    DisposableEffect(Unit) {
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(connectivityReceiver, intentFilter)

        onDispose {
            context.unregisterReceiver(connectivityReceiver)
        }
    }
}