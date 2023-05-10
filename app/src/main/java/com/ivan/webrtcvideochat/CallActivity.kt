package com.ivan.webrtcvideochat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ivan.webrtcvideochat.Fragments.CallsFragment
import com.ivan.webrtcvideochat.Fragments.HomeFragment
import com.ivan.webrtcvideochat.Helper.JavascriptInterface_
import com.ivan.webrtcvideochat.databinding.ActivityCallBinding

class CallActivity : AppCompatActivity() {
    private lateinit var binding:ActivityCallBinding
    var databaseReference = FirebaseDatabase.getInstance().getReference("USERS")
    var isConnected = false
    var isAudio = true
    var isVideo = true

    var targetUserName:String = ""
    var targetUserId:String=""
    var sourceUsername : String = ""
    var sourceUserId:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)

        setContentView(binding.root)

        //get the intent
        //--
        var isCalling = intent.getBooleanExtra("callingStatus",false)
        sourceUsername = intent.getStringExtra("sourceUserName")!!
        sourceUserId = intent.getStringExtra("sourceUserId")!!
        targetUserName = intent.getStringExtra("targetUserName")!!
        targetUserId = intent.getStringExtra("targetUserId")!!

        if(isCalling){
            sendCallRequest()
        }


        binding.toggleAudioBtn.setOnClickListener {
            isAudio = !isAudio

            callJavascriptFunction("javascript:toggleAudio(\"${isAudio}\")")

            binding.toggleAudioBtn.setImageResource(

                if(isAudio) R.drawable.ic_baseline_mic_24
                else R.drawable.ic_baseline_mic_off_24
            )
        }


        binding.toggleVideoBtn.setOnClickListener {
            isVideo = !isVideo
            callJavascriptFunction("javascript:toggleVideo(\"${isVideo}\")")

            binding.toggleVideoBtn.setImageResource(
                if(isVideo)R.drawable.ic_baseline_videocam_24
                else R.drawable.ic_baseline_videocam_off_24
            )
        }


        setUpWebView()
    }

    private fun setUpWebView() {

        binding.webView.webChromeClient = object : WebChromeClient(){
            override fun onPermissionRequest(request: PermissionRequest?) {
                super.onPermissionRequest(request)
                request?.grant(request.resources)
            }

        }

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.mediaPlaybackRequiresUserGesture = false
        binding.webView.addJavascriptInterface(JavascriptInterface_(this),"Android")

        loadVideoCall()
    }

    private fun loadVideoCall() {
        val filePath = "file:android_asset/call.html"

        binding.webView.loadUrl(filePath)
        binding.webView.webViewClient = object: WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                initializePeer()
            }
        }
    }

    private fun initializePeer() {
        callJavascriptFunction("javascript:init(\"${sourceUserId}\")")

        databaseReference.child(sourceUsername)
            .child("incoming").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                        onCallRequest(snapshot as? String)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun onCallRequest(caller: String?) {
            if(caller?.isEmpty()!!)return
            binding.callLayout.visibility =View.VISIBLE
            binding.incomingCallTxt.text = "$caller is calling..."


            binding.acceptBtn.setOnClickListener {
                databaseReference.child(sourceUserId)
                    .child("isAvailable").setValue(true)

                binding.callLayout.visibility = View.GONE
                switchToControls()
            }

            binding.rejectBtn.setOnClickListener {
                databaseReference.child(sourceUserId).child("isAvailable").setValue(false)
                databaseReference.child(sourceUserId).child("incoming").setValue("")
                binding.callLayout.visibility = View.GONE

                startActivity(Intent(this,CallsFragment::class.java))
                finish()
            }
    }

    private fun callJavascriptFunction(functionName: String) {
        binding.webView.post{
            binding.webView.evaluateJavascript(functionName,null)
        }
    }

    override fun onDestroy() {

        databaseReference.child(targetUserId).child("isAvailable").setValue(false)
        databaseReference.child(targetUserId).child("incoming").setValue("")

        binding.webView.loadUrl("about:blank")
        super.onDestroy()

    }

    private fun sendCallRequest(){
        if(!isConnected){
            Toast.makeText(this,"Not connected to the internet!",Toast.LENGTH_SHORT)
                .show()
            return
        }

        databaseReference.child(targetUserId).child("incoming").setValue(sourceUsername)
        databaseReference.child(targetUserId).child("isAvailable")
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value.toString() == "true"){
                        listenForConnectionId()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun listenForConnectionId() {
        databaseReference.child(targetUserId).addValueEventListener(
            object:ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        switchToControls()
                        callJavascriptFunction("javascript:startCall(\"${snapshot.value}\")")
                    }else{
                       Toast.makeText(this@CallActivity,"No user named $targetUserName",Toast.LENGTH_SHORT)
                           .show()
                        return
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            }
        )
    }

    private fun switchToControls() {

        binding.callControlLayout.visibility = View.VISIBLE
    }
}