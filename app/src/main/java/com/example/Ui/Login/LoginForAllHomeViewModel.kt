package com.example.Ui.Login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ryte.Services.FCMService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
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
class LoginForAllHomeViewModel @Inject constructor() : ViewModel() {
    var theTypeOfUser:String?=null
    var erorr:MutableLiveData<String> =MutableLiveData()
    val loginSuccess:MutableLiveData<Boolean> = MutableLiveData()

    //cap log in
    var capEmail:String?=null
    var capPass:String?=null


    //cst login
var  thePhoneNumber:String?=null
var  theCodeRecieved:String?=null
var theCodeSent:String?=null
var codeSentAlreay:MutableLiveData<Boolean> =MutableLiveData()
var cstExist:MutableLiveData<Boolean> =MutableLiveData()

    // admin login
    var adminEmail:String?=null
    var adminPass:String?=null
    var isAdmin:MutableLiveData<Boolean> =MutableLiveData()

    fun checkIfCstExit()
    {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("Customers").child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists())
                            {
                                viewModelScope.launch(Dispatchers.Main){
                                    cstExist.value= true
                                }
                            }
                            else
                            {
                                viewModelScope.launch(Dispatchers.IO){
                                    createANewCustomerDetails()
                                    updateTheuserTypeToBeCst()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
        }
    }
    private fun  createANewCustomerDetails(){
        thePhoneNumber?.let {
            val theMap = HashMap<String, Any>()
            theMap["uid"] =  FirebaseAuth.getInstance().currentUser!!.uid
            theMap["Image"] = "https://firebasestorage.googleapis.com/v0/b/ryte-305306.appspot.com/o/ProfilePicDefault%2Fuser.png?alt=media&token=6c6c1a84-3d6f-45a6-abcd-f7412bc2ac21"
            theMap["Name"] = "Rider"
            theMap["Status"] = "Ryte"
            theMap["Phone"] = it
            theMap["Credit"] = "5"
            FirebaseDatabase.getInstance().reference.child("Customers").child( FirebaseAuth.getInstance().currentUser!!.uid).updateChildren(theMap)

        }


    }

    private fun updateTheuserTypeToBeCst()=FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue("Cst").addOnSuccessListener {
        viewModelScope.launch(Dispatchers.Main){
            withContext(Dispatchers.IO){
                FCMService.updateTheCurrentUsertoken()
            }
            cstExist.value= true
        }
    }

    fun checkIfAdmin()=viewModelScope.launch(Dispatchers.IO) {
        FirebaseDatabase.getInstance().reference.child("Admins")
                .child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists())
                        {
                            viewModelScope.launch(Dispatchers.Main) {
                                isAdmin.value=true
                            }
                        }
                        else
                        {
                            viewModelScope.launch(Dispatchers.Main) {
                                isAdmin.value=false

                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
    }

    fun loginForCap()=  FirebaseAuth.getInstance().signInWithEmailAndPassword(capEmail!!, capPass!!).addOnSuccessListener {
        viewModelScope.launch(Dispatchers.Main) {
            loginSuccess.value=true
        }
    }.addOnFailureListener {
        viewModelScope.launch(Dispatchers.Main) {
            loginSuccess.value=false
            erorr.value=it.message
        }
    }

    fun loginForAdmin()=   FirebaseAuth.getInstance().signInWithEmailAndPassword(adminEmail!!, adminPass!!).addOnSuccessListener {
        viewModelScope.launch(Dispatchers.Main) {
            loginSuccess.value=true
        }
    }.addOnFailureListener {
        viewModelScope.launch(Dispatchers.Main) {
            loginSuccess.value=false
            erorr.value=it.message
        }
    }
    fun verifyTheNewCustomer() {

          theCodeRecieved?.let {
            FirebaseAuth.getInstance().signInWithCredential(PhoneAuthProvider.getCredential(theCodeSent!!, theCodeRecieved!!)).addOnSuccessListener {
                loginSuccess.value=true
            }.addOnFailureListener {
                loginSuccess.value=false
                erorr.value=it.message
            }
        }



    }


}