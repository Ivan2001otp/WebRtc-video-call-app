package com.ivan.webrtcvideochat.Fragments

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ivan.webrtcvideochat.Helper.ConnectivityInterfaceObserver
import com.ivan.webrtcvideochat.Helper.NetworkConnectivityObserver
import com.ivan.webrtcvideochat.Helper.RemoteRepository
import com.ivan.webrtcvideochat.R
import com.ivan.webrtcvideochat.databinding.FragmentHomeBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onEach


class HomeFragment : Fragment() {

    private var _binding : FragmentHomeBinding ?= null
    private val binding get() = _binding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUserId : String
    private lateinit var remoteRepository: RemoteRepository
    private lateinit var imgView_profile:ImageView
    private lateinit var onInternetObserver:ConnectivityInterfaceObserver
    private  lateinit var connectivityManager: ConnectivityManager

    private val permissions = arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.RECORD_AUDIO)
    private val  REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
            // Inflate the layout for this fragment
            remoteRepository = RemoteRepository()

            databaseReference = FirebaseDatabase.getInstance().getReference("USERS")
            firebaseAuth = FirebaseAuth.getInstance()
            currentUserId = firebaseAuth.currentUser!!.uid

            _binding = FragmentHomeBinding.inflate(inflater,container,false)
            return binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imgView_profile = view.findViewById(R.id.profileImg)


        connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //check internet is available
        onInternetObserver = NetworkConnectivityObserver(requireContext())

        onInternetObserver.onInternetConnectivity().onEach {internetStatus->
            val netCapability  = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

            if(netCapability ==  null &&
                !connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)!!.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            {
                AlertDialog.Builder(requireContext())
                    .setTitle("Poor Internet connection")
                    .setMessage("Please Make sure you have active internet .")
                    .setCancelable(false)
                    .setPositiveButton("Refresh"){dialogInterface,it->
                        connectivityManager.bindProcessToNetwork(null)
                        dialogInterface.dismiss()
                    }
                    .show()
            }
        }

        //asking manifest pemrsm
        Log.d("perm", "permission grant : ${isPermissionGranted()}")
        if(!isPermissionGranted()){
            askPermission()
        }


        getUserDetails(userId = currentUserId)

    }



    public fun getUserDetails(userId:String){

        var name:String ?= null
        var phonenum:String?=null
        var userUniqueId:String?=null

        val database = FirebaseDatabase.getInstance().getReference("USERS")

        database.child(userId)
            .get().addOnSuccessListener {
                if(it.exists()){
                    name = it.child("userName").value.toString()
                    userUniqueId = it.child("userId").value.toString()
                    phonenum = it.child("phNumber").value.toString()

                    val img :String = it.child("imgUrl").value.toString()


                    Picasso.get()
                        .load(Uri.parse(img))
                        .placeholder(R.drawable.user_avater)
                        .into(imgView_profile)


                    binding!!.userId.text = userUniqueId
                    binding!!.usernameTv.text = name
                    binding!!.PhoneNumber.text = phonenum
                }
            }.addOnFailureListener{
                Toast.makeText(requireContext(),"Connection Timed out.",Toast.LENGTH_SHORT)
                    .show()
            }


    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(requireActivity(),permissions,REQUEST_CODE)
    }

    private fun isPermissionGranted(): Boolean {
        permissions.forEach {
            if(ActivityCompat.checkSelfPermission(requireContext(),it)!= PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true
    }

}