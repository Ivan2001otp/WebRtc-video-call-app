package com.ivan.webrtcvideochat.Helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class NetworkConnectivityObserver(private val context: Context) : ConnectivityInterfaceObserver {

    private val connectivityManager  = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun onInternetConnectivity(): Flow<ConnectivityInterfaceObserver.Status> {
        return callbackFlow {
            val callBack = object : ConnectivityManager.NetworkCallback(){


                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch {
                        send(ConnectivityInterfaceObserver.Status.Available)
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch {
                        send(ConnectivityInterfaceObserver.Status.Unavailable)
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch {
                        send(ConnectivityInterfaceObserver.Status.Lost)
                    }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch {
                        send(ConnectivityInterfaceObserver.Status.Losing)
                    }
                }

            }
        }
    }
}