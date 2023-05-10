package com.ivan.webrtcvideochat

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.ivan.webrtcvideochat.Fragments.CallsFragment
import com.ivan.webrtcvideochat.Fragments.HomeFragment
import com.ivan.webrtcvideochat.Intro.PhoneActivity
import com.ivan.webrtcvideochat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var mauth:FirebaseAuth

//    private val permissions = arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.RECORD_AUDIO)
//    private val  REQUEST_CODE = 1

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.signOut->{

                mauth.signOut()
                startActivity(Intent(this@MainActivity,PhoneActivity::class.java))
                finishAffinity()
            }
            R.id.settings->{

                startActivity(Intent(this@MainActivity,SettingsActivity::class.java))
                finish()
            }

            else->{}
        }
        return true
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mauth = FirebaseAuth.getInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        //asking manifest pemrsm
//        if(isPermissionGranted()){
//            askPermission()
//        }


        replaceFragment(HomeFragment())

        binding.bottomlayout.setOnItemSelectedListener {
            when(it.itemId){
                R.id.profile->{replaceFragment(HomeFragment())}
                R.id.Calls->{replaceFragment(CallsFragment())}
                else->{

                }
            }
            true
        }
    }

    /*private fun askPermission() {
        ActivityCompat.requestPermissions(this,permissions,REQUEST_CODE)
    }

    private fun isPermissionGranted(): Boolean {
        permissions.forEach {
            if(ActivityCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true
    }*/

    private fun replaceFragment(targetF: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.targetFrameLayout,targetF)
        fragmentTransaction.commit()
    }


}