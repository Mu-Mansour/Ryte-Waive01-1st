package com.example.Ui.AdminAddNewCap

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminAddCapViewModel @Inject constructor() : ViewModel() {
    var name:String?=null
    var email:String?=null
    var nationalId:String?=null
    var zone:String?=null
    var licence:String?=null
    var phone:String?=null
    var category:String?=null
    var model:String?=null
    var imageURI: Uri?= null
    var allTasksAreDone:MutableLiveData<Boolean> = MutableLiveData()

    fun addNewCaptain()
    {
        viewModelScope.launch(Dispatchers.IO) {
            val theCaptinMap = HashMap<String, Any>()
            theCaptinMap["name"] = name!!
            theCaptinMap["email"] = email!!
            theCaptinMap["nationalId"] = nationalId!!
            theCaptinMap["zone"] = zone!!
            theCaptinMap["category"] = category!!
            theCaptinMap["model"] = model!!
            theCaptinMap["licence"] = licence!!
            theCaptinMap["phone"] = phone!!
            theCaptinMap["Status"] = "OFF"
            FirebaseAuth.getInstance().createUserWithEmailAndPassword("${theCaptinMap["email"]}", "111111")
                    .addOnSuccessListener {
                        FirebaseDatabase.getInstance().reference.child("Captains").child(it.user!!.uid).updateChildren(theCaptinMap).addOnCompleteListener { it1 ->
                            if (it1.isSuccessful) {
                                val imageFileRefrence = FirebaseStorage.getInstance().reference.child("CaptainsImages").child("${it.user!!.uid}.png")
                                imageFileRefrence.putFile(imageURI!!).addOnCompleteListener { thefirstTask ->
                                    if (thefirstTask.isSuccessful) {
                                        imageFileRefrence.downloadUrl.addOnSuccessListener { theLink ->
                                            FirebaseDatabase.getInstance().reference.child("Captains").child(it.user!!.uid).child("image").setValue(theLink.toString()).addOnCompleteListener { thePreFinalTask ->

                                                if (thePreFinalTask.isSuccessful) {
                                                    FirebaseDatabase.getInstance().reference.child("Users").child(it.user!!.uid).setValue("Cap").addOnCompleteListener { thefinalTask ->
                                                        if (thefinalTask.isSuccessful) {
                                                            FirebaseDatabase.getInstance().reference.child("CaptainWallets").child(it.user!!.uid).setValue("0").addOnSuccessListener {
                                                                theCaptinMap.clear()
                                                                viewModelScope.launch(Dispatchers.Main) {
                                                                    allTasksAreDone.value=true
                                                                }

                                                            }
                                                        }

                                                    }

                                                }


                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
        }

    }

}