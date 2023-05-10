package com.ivan.webrtcvideochat.Helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ivan.webrtcvideochat.Model.User

class SharedViewModel : ViewModel(){

    private val _freshUserList  = MutableLiveData<List<User>>()

    //val recipeList : LiveData<List<Recipe>> = _recipeList

    val freshUserLiveData : LiveData<List<User>> = _freshUserList

    fun onUpdateUserListModel(userList:List<User>){
        _freshUserList.value = userList
    }

    fun getUpdatedUserList():LiveData<List<User>>{
        return freshUserLiveData
    }

}