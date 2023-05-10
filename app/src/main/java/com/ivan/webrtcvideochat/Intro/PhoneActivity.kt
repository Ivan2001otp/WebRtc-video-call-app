package com.ivan.webrtcvideochat.Intro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ivan.webrtcvideochat.Helper.RemoteRepository
import com.ivan.webrtcvideochat.MainActivity
import com.ivan.webrtcvideochat.R
import com.ivan.webrtcvideochat.databinding.ActivityPhoneBinding
import com.ivan.webrtcvideochat.databinding.FragmentCallsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PhoneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var number:String
    private lateinit var userName:String
    private lateinit var dbReference:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneBinding.inflate(layoutInflater)

        dbReference = Firebase.database.reference

        setContentView(binding.root)
        init()



        binding.sendOTPBtn.setOnClickListener {
            userName = binding.usernameEt.text.trim().toString()
            number = binding.phoneEditTextNumber.text.toString()

            Log.d("tag", "number is : ${number}")
            Log.d("tag", "username  is : ${userName}")


            if(number.isNotEmpty() and userName.isNotEmpty()){
                Log.d("tag", "number length : ${number.length}")

                if(number.length==10){
                    val temp_num:String = "+91$number"
                    binding.phoneProgressBar.visibility = View.VISIBLE

                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(temp_num)
                        .setTimeout(60L,TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build()

                    PhoneAuthProvider.verifyPhoneNumber(options)

                }else{
                    showToast("Please enter correct number.")
                }
            }else{
                showToast("Enter your credentials.")
            }
        }
    }

    private fun sendToMain(){

        CoroutineScope(Dispatchers.IO).launch {
            val id:String = auth.currentUser!!.uid
            RemoteRepository().writeUserData(userName,id,number,dbReference)
        }

        startActivity(Intent(this,MainActivity::class.java))
    }

    private val callbacks=object:PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
                e.printStackTrace()
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
                e.printStackTrace()
            }
            binding.phoneProgressBar.visibility = View.VISIBLE
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            binding.phoneProgressBar.visibility = View.INVISIBLE

            val intent = Intent(this@PhoneActivity , OtpActivity::class.java)
            intent.putExtra("OTP" , verificationId)
            intent.putExtra("resendToken" , token)
            intent.putExtra("phoneNumber" , number)
            intent.putExtra("username_1",userName)
            startActivity(intent)
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this){task->
                if(task.isSuccessful){
                    showToast("Authenticated Successfully.")
                    sendToMain()
                }else{
                    Log.d("tag", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }

                binding.phoneProgressBar.visibility = View.INVISIBLE
            }
    }

    private fun init(){
        binding.phoneProgressBar.visibility = View.INVISIBLE
        auth = FirebaseAuth.getInstance()
    }

    private fun showToast(message:String){
        Toast.makeText(this ,message,Toast.LENGTH_SHORT)
            .show()
    }

    override fun onStart() {
        super.onStart()

        if(auth.currentUser!=null){
            startActivity(Intent(this,MainActivity::class.java))
        }
    }
}