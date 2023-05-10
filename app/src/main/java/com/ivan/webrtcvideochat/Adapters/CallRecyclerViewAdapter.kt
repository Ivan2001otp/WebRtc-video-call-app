package com.ivan.webrtcvideochat.Adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ivan.webrtcvideochat.Helper.OnCallClick
import com.ivan.webrtcvideochat.Model.User
import com.ivan.webrtcvideochat.R
import com.squareup.picasso.Picasso

class CallRecyclerViewAdapter(private val context: Context, private val userList:List<User>,val onCallClick: OnCallClick
) : RecyclerView.Adapter<CallRecyclerViewAdapter.CallViewHolder>(){

    private var oldUserList :List<User> = emptyList()
    
    fun setData(newUserList:List<User>){
         val diffUtil = CustomDiffUtil(oldUserList,newUserList)
        val difUtilResult =  DiffUtil.calculateDiff(diffUtil)
        oldUserList = newUserList
        difUtilResult.dispatchUpdatesTo(this)
    }


    inner class CallViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) , View.OnClickListener{

        init{
            itemView.findViewById<Button>(R.id.row_call_btn).setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            val selectedUser :User = oldUserList[adapterPosition]
            when(v?.id){

               R.id.row_call_btn->{
                    onCallClick.onItemCallClickListener(selectedUser)
               }else->{
                   //nothing
               }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {

        val view : View =LayoutInflater.from(parent.context)
            .inflate(R.layout.todo_row_call_layout,parent,false)

        return CallViewHolder(view)
    }

    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {

        holder.itemView.apply {
            this.findViewById<TextView>(R.id.row_username_tv).text = userList[position].userName
        }

        holder.itemView.apply {

        }

        val uri_img = Uri.parse(userList.get(position).imgUrl)

        val imgView:ImageView = holder.itemView.findViewById(R.id.row_profile_pic)
        Picasso.get()
            .load(uri_img)
            .placeholder(R.drawable.user_avater)
            .resize(75,75)
            .centerCrop()
            .into(imgView)
    }

    override fun getItemCount(): Int {
        return userList.count()
    }
}