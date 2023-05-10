package com.ivan.webrtcvideochat

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.ivan.webrtcvideochat.Helper.RemoteRepository
import com.ivan.webrtcvideochat.Model.User
import com.ivan.webrtcvideochat.databinding.ActivitySettingsBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySettingsBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var currentUserId:String
    private lateinit var  database:FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        binding = ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

        val userId: String = FirebaseAuth.getInstance().currentUser!!.uid

        val db : DatabaseReference = FirebaseDatabase.getInstance().reference

        //updating the profile photo
        binding.changePhotoBtn.setOnClickListener {
            val intent: Intent = Intent()
            intent.setAction(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")//for images

            //invoke the launcher
            GalleryPhotoPickerContract.launch("image/*")
        }

        //setting home intent
        binding.homeArrBtn.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

        //working with user name and phone num
        binding.saveBtn.setOnClickListener {
            val username:String  = binding.usernameEt.text?.trim().toString()
            val phNum:String  = binding.phoneEt.text?.trim().toString()

           if(username.isNotEmpty() and phNum.isNotEmpty()){
               //write the to firebase
               MainScope().launch {
                   RemoteRepository().updateProfile(username,userId,phNum,db)
               }
           }else{
               Toast.makeText(this,"Please enter your Credentials correctly.",Toast.LENGTH_SHORT)
                   .show()
           }
        }
    }

    private val GalleryPhotoPickerContract = registerForActivityResult(ActivityResultContracts.GetContent()){
        //save it in firebase storage
        //display it from the firebase storage.


            try{
                if(it != null){
                    binding.profileImage.setImageURI(it)

                    val referenc_storage:StorageReference = storage.getReference().child(("PROFILE_PICS"))
                        .child(currentUserId)


                    database.getReference().child("USERS")
                        .child(currentUserId)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                        val user: User = snapshot.getValue(User::class.java)!!

                                if(user.imgUrl.isNotEmpty()){
                                    Picasso.get()
                                        .load(user.imgUrl)
                                        .placeholder(R.drawable.user_avater)
                                        .into(binding.profileImage)
                                }



                                binding.usernameEt.setText(user.userName)
                                binding.phoneEt.setText(user.phNumber)
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })

                    referenc_storage.putFile(it).addOnSuccessListener {
                        referenc_storage.downloadUrl.addOnSuccessListener { uri -> //write the url to realtime database
                            database.getReference().child("USERS")
                                .child(currentUserId)
                                .child("imgUrl").setValue(uri.toString())

                            Toast.makeText(this@SettingsActivity,"Profile Updated Successfully",Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                }
            }catch (e:Exception){
                e.printStackTrace()
            }

    }



}