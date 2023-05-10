package com.ivan.webrtcvideochat.Model

class User(val userId:String,val userName:String,val phNumber:String,val imgUrl:String) {

    constructor() : this("","","",""){

    }

    var isAvailable : Boolean = false
        get()=isAvailable
        set(value){
            field = value
        }

    var incoming:String = ""
        get() = incoming
        set(value){
            field = value
        }
}