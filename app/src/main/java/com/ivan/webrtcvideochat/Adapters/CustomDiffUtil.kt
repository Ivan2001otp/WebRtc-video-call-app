package com.ivan.webrtcvideochat.Adapters

import androidx.recyclerview.widget.DiffUtil
import com.ivan.webrtcvideochat.Model.User

class CustomDiffUtil(
    private val newUserList:List<User>,
    private val oldUserList:List<User>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldUserList.size
    }

    override fun getNewListSize(): Int {
        return newUserList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldUserList[oldItemPosition].userId == newUserList[newItemPosition].userId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldUserList[oldItemPosition].hashCode() == newUserList[oldItemPosition].hashCode()
    }
}