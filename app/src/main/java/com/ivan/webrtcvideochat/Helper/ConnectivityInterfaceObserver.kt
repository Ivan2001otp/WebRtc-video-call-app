package com.ivan.webrtcvideochat.Helper

import kotlinx.coroutines.flow.Flow

interface ConnectivityInterfaceObserver {
    fun onInternetConnectivity() : Flow<Status>


    enum class  Status{
        Available,Unavailable,Losing,Lost
    }
}