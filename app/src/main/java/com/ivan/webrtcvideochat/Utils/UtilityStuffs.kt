package com.ivan.webrtcvideochat.Utils

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import com.ivan.webrtcvideochat.Model.User
import kotlin.math.log

class UtilityStuffs {

   companion object{

       fun dialogBoxBuilder(context: Context,targetUser: User):AlertDialog.Builder{

           val builder = AlertDialog.Builder(context)

            builder.setTitle("Please confirm !")
                .setMessage("Do you want to call ${targetUser.userName}")
                .setPositiveButton("Yes"){dialogInterface,it->

                    Log.d("call", "dialogBoxBuilder: ${targetUser.userName}")
                    Log.d("call", "dialogBoxBuilder: ${targetUser.phNumber}")
                    Log.d("call", "dialogBoxBuilder: ${targetUser.userId}")

                }.setNegativeButton("No"){dialogInterface,it->

                }
           return builder
       }
   }
}