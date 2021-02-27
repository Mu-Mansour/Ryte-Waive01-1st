package com.example.Ui.Splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ryte.Others.Utility
import com.example.ryte.Services.FCMService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {
   val theUser :MutableLiveData<Boolean> = MutableLiveData()
   val theUserType :MutableLiveData<String> = MutableLiveData()

    fun checkUser() {
        if (FirebaseAuth.getInstance().currentUser!= null)
        {
            viewModelScope.launch(Dispatchers.IO) {
                FCMService.updateTheCurrentUsertoken()
                FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance()
                    .currentUser!!.uid).addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists())
                        {
                            theUserType.value=snapshot.value.toString()
                            Utility.theUserType=snapshot.value.toString()
                            FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance()
                                .currentUser!!.uid).removeEventListener(this)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
                withContext(Dispatchers.Main){
                    theUser.value=true
                }
            }


        }
        else
        {
            theUser.value=false
        }
    }
    fun signOutProcess()
    {
        FirebaseAuth.getInstance().signOut()
        FirebaseDatabase.getInstance().reference
    }
}