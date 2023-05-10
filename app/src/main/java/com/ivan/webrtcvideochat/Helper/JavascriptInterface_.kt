package com.ivan.webrtcvideochat.Helper

import android.webkit.JavascriptInterface
import com.ivan.webrtcvideochat.CallActivity

class JavascriptInterface_(val callActivity: CallActivity) {


    @JavascriptInterface
    public fun onPeerConnected(){
        callActivity.isConnected
    }
}