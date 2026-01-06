package com.example.pubmanager.ui.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberIsOnline(): State<Boolean> {
    val context = LocalContext.current
    val cm = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    val state = remember { mutableStateOf(isOnline(context)) }

    DisposableEffect(cm) {
        val request = NetworkRequest.Builder().build()

        val callback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                state.value = isOnline(context)
            }

            override fun onLost(network: Network) {
                state.value = isOnline(context)
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                state.value = isOnline(context)
            }
        }

        cm.registerNetworkCallback(request, callback)
        onDispose { cm.unregisterNetworkCallback(callback) }
    }

    return state
}
