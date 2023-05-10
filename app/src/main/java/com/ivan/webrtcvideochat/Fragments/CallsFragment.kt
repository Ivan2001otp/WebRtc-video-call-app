package com.ivan.webrtcvideochat.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ivan.webrtcvideochat.Adapters.CallRecyclerViewAdapter
import com.ivan.webrtcvideochat.CallActivity
import com.ivan.webrtcvideochat.Helper.OnCallClick
import com.ivan.webrtcvideochat.Helper.RemoteRepository
import com.ivan.webrtcvideochat.Helper.SharedViewModel
import com.ivan.webrtcvideochat.Model.User
import com.ivan.webrtcvideochat.R
import com.ivan.webrtcvideochat.Utils.UtilityStuffs
import com.ivan.webrtcvideochat.databinding.FragmentCallsBinding
import kotlinx.coroutines.*


class CallsFragment : Fragment() , OnCallClick {

    private var _binding : FragmentCallsBinding ?= null
    private val binding get() = _binding
    private lateinit var  mauth:FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var currUserId : String
    private lateinit var callAdapter:CallRecyclerViewAdapter
    private  lateinit var responseList : List<User>

    private val sharedViewModel : SharedViewModel  by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        _binding = FragmentCallsBinding.inflate(inflater,container,false)

        return binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //fetch all user data from firebase
        mauth = FirebaseAuth.getInstance()

        databaseReference = FirebaseDatabase.getInstance().getReference("USERS")
        currUserId = mauth.currentUser!!.uid

        binding!!.recyclerView.setHasFixedSize(true)
        val layout_Manager = LinearLayoutManager(context)
        layout_Manager.orientation = LinearLayoutManager.VERTICAL
        binding!!.recyclerView.layoutManager = layout_Manager

        responseList = emptyList()

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO){
                try{
                    responseList = RemoteRepository().getAllUserCallList(databaseReference,currUserId)

                    Log.e("users", "onViewCreated: ${responseList.get(0).userName} and ${responseList.size}" )
                }catch (e : Exception){
                    Log.e("users", "error mess: ${e.message}" )
                    e.printStackTrace()
                }

                withContext(Dispatchers.Main){
                    if(responseList.isNotEmpty()){
                        sharedViewModel.onUpdateUserListModel(responseList)
                    }

                    sharedViewModel.freshUserLiveData.observe(viewLifecycleOwner){
                        Log.e("users", "inside adapter invokation : ${responseList.get(0).userName} and ${responseList.size}" )

                        if(it.size==0){
                            Toast.makeText(requireContext(),"Poor internet or no list of users!",Toast.LENGTH_SHORT)
                                .show()
                        }else {


                            callAdapter =
                                CallRecyclerViewAdapter(requireContext(), it, this@CallsFragment)
                            callAdapter.setData(it)
                            binding!!.recyclerView.adapter = callAdapter
                        }
                    }

                }
            }
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onItemCallClickListener(user: User) {
        //val dialogBuilder = UtilityStuffs.dialogBoxBuilder(requireContext(),user)
        //dialogBuilder.show()

        val intent = Intent(requireContext(),CallActivity::class.java)
        /*
        sourceUserName
        sourceUserId
        targetUserName
        targetUserId
        callingStatus
         */

        var srcUserName :String = ""
        var srcUerId :String = ""
        databaseReference.child(currUserId)
            .get()
            .addOnSuccessListener {
                if(it.exists()){
                   srcUserName =  it.child("userName").toString()
                    srcUerId = it.child("userId").toString()
                }
            }

        intent.putExtra("targetUserName",user.userName)
        intent.putExtra("targetUserId",user.userId)
        intent.putExtra("sourceUserId",srcUerId)
        intent.putExtra("sourceUserName",srcUserName)

        startActivity(intent)

        Toast.makeText(requireContext(),"Clicked ${user.userName}",Toast.LENGTH_SHORT)
            .show()
    }
}