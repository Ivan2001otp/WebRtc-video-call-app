package com.ivan.webrtcvideochat.Intro

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ivan.webrtcvideochat.Helper.RemoteRepository
import com.ivan.webrtcvideochat.MainActivity
import com.ivan.webrtcvideochat.R
import com.ivan.webrtcvideochat.databinding.ActivityOtpBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {
    private lateinit var inputOTP1: EditText
    private lateinit var inputOTP2: EditText
    private lateinit var inputOTP3: EditText
    private lateinit var inputOTP4: EditText
    private lateinit var inputOTP5: EditText
    private lateinit var inputOTP6: EditText
    private lateinit var binding: ActivityOtpBinding
    private lateinit var dbReference: DatabaseReference

    private lateinit var auth:FirebaseAuth

    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String
    private lateinit var u_name:String
    private lateinit var remoteRepository: RemoteRepository
    private lateinit var C_phoneNumber : String

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        dbReference = Firebase.database.reference
        remoteRepository = RemoteRepository()

        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inputOTP1=binding.otpEditText1
        inputOTP2=binding.otpEditText2
        inputOTP3=binding.otpEditText3
        inputOTP4=binding.otpEditText4
        inputOTP5=binding.otpEditText5
        inputOTP6=binding.otpEditText6

        OTP = intent.getStringExtra("OTP").toString()

        resendToken = intent.getParcelableExtra<PhoneAuthProvider.ForceResendingToken>("resendToken")!!

        phoneNumber = intent.getStringExtra("phoneNumber")!!
        C_phoneNumber = "+91"+phoneNumber
        u_name = intent.getStringExtra("username_1")!!

        binding.otpProgressBar.visibility= View.INVISIBLE
        addTextChangeListener()
        resendOtpTvVisibility()

        binding.resendTextView.setOnClickListener {
            resendVerificationCode()
            resendOtpTvVisibility()
        }

        binding.verifyOTPBtn.setOnClickListener {
            val typedOTP =
                (inputOTP1.text.toString()) + (inputOTP2.text.toString()) +(inputOTP3.text.toString()) +
                        (inputOTP4.text.toString()) +(inputOTP5.text.toString()) +(inputOTP6.text.toString())

            if(typedOTP.isNotEmpty()){
                if(typedOTP.length==6){
                    val credentials:PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        OTP,typedOTP
                    )

                    binding.otpProgressBar.visibility = View.VISIBLE
                    signInWithPhoneAuthCredential(credentials)
                }else{
                    Toast.makeText(this,"Please Enter correct otp.",Toast.LENGTH_SHORT)
                        .show()

                }
            }else{
                Toast.makeText(this,"Please enter OTP.",Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(C_phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

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
            binding.otpProgressBar.visibility = View.VISIBLE
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            OTP = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information

                    Toast.makeText(this, "Authenticate Successfully", Toast.LENGTH_SHORT).show()
                    sendToMain()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
                binding.otpProgressBar.visibility = View.VISIBLE
            }
    }

    private fun sendToMain() {

        MainScope().launch {
            val id:String = auth.currentUser!!.uid
            remoteRepository.writeUserData(u_name,id,phoneNumber,dbReference)
        }
        startActivity(Intent(this, MainActivity::class.java))
    }
    private fun resendOtpTvVisibility() {
        inputOTP1.setText("")
        inputOTP2.setText("")
        inputOTP3.setText("")
        inputOTP4.setText("")
        inputOTP5.setText("")
        inputOTP6.setText("")

        binding.resendTextView.visibility = View.INVISIBLE
        binding.resendTextView.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed(Runnable{
            binding.resendTextView.visibility = View.VISIBLE
            binding.resendTextView.isEnabled = true
        },60000)
    }

    private fun addTextChangeListener() {
        inputOTP1.addTextChangedListener(EditTextWatcher(inputOTP1))
        inputOTP2.addTextChangedListener(EditTextWatcher(inputOTP2))
        inputOTP3.addTextChangedListener(EditTextWatcher(inputOTP3))
        inputOTP4.addTextChangedListener(EditTextWatcher(inputOTP4))
        inputOTP5.addTextChangedListener(EditTextWatcher(inputOTP5))
        inputOTP6.addTextChangedListener(EditTextWatcher(inputOTP6))

    }


    inner class EditTextWatcher(private val view:View):TextWatcher{
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            val text = p0.toString()
            when (view.id) {
                R.id.otpEditText1 -> if (text.length == 1) inputOTP2.requestFocus()
               R.id.otpEditText2 -> if (text.length == 1) inputOTP3.requestFocus() else if (text.isEmpty()) inputOTP1.requestFocus()
                R.id.otpEditText3 -> if (text.length == 1) inputOTP4.requestFocus() else if (text.isEmpty()) inputOTP2.requestFocus()
                R.id.otpEditText4 -> if (text.length == 1) inputOTP5.requestFocus() else if (text.isEmpty()) inputOTP3.requestFocus()
                R.id.otpEditText5 -> if (text.length == 1) inputOTP6.requestFocus() else if (text.isEmpty()) inputOTP4.requestFocus()
                R.id.otpEditText6 -> if (text.isEmpty()) inputOTP5.requestFocus()

            }
        }


    }


}