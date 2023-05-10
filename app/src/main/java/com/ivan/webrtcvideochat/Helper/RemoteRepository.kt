package com.ivan.webrtcvideochat.Helper

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.WorkerThread
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ivan.webrtcvideochat.Model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RemoteRepository {

    @WorkerThread
    suspend fun writeUserData(username:String,userId:String,number:String,db:DatabaseReference){
        val u = User(
           userId,
            username,
            number,
            ""
        )
        db.child("USERS")
            .child(userId)
            .setValue(u)

    }

    @WorkerThread
    suspend fun uploadProfileImg(userId:String, imgUrl: String, db:DatabaseReference) = withContext(Dispatchers.IO){
        db.child("USERS")
            .child(userId)
            .child("imgUrl").setValue(imgUrl)
    }


    @WorkerThread
    suspend fun updateProfile(username:String,userId:String,number:String,db:DatabaseReference){

       var hashmap : HashMap<String,Any> = HashMap()
        hashmap.put("userName",username)
        hashmap.put("userId",userId)
        hashmap.put("phNumber",number)

        db.child("USERS")
            .child(userId)
            .updateChildren(hashmap)
    }



    suspend fun  getAllUserCallList(db:DatabaseReference,userId:String) : List<User> = withContext(Dispatchers.IO){
            var item : User ?= null

            val responseList = db.get().await().children.mapNotNull {
                item = it.getValue(User::class.java)
                if (item!!.userId!=userId && item != null)
                {
                    item
                }else{
                    null
                }

            }

         responseList
    }


}