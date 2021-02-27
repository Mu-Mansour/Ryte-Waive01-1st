package com.example.Ui.Profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ryte.Logic.Captain
import com.example.ryte.Logic.Customer
import com.example.ryte.Logic.PendingRideModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    val rides:MutableLiveData<List<PendingRideModule>> = MutableLiveData()


    //for Caps
    val captainDetails:MutableLiveData<Captain> = MutableLiveData()





    //fo csts
    val cstDetails:MutableLiveData<Customer> = MutableLiveData()

    fun getTheTotalRides(){
        //FirebaseDatabase.getInstance().reference
    }


    fun getTheUserData(userType:String) {
        viewModelScope.launch(Dispatchers.IO) {

            if (userType == "Cst") {

                FirebaseDatabase.getInstance().reference.child("Customers")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                viewModelScope.launch(Dispatchers.Main){
                                    cstDetails.value = snapshot.getValue(Customer::class.java)

                                }

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            } else {
                FirebaseDatabase.getInstance().reference.child("Captains")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                viewModelScope.launch(Dispatchers.Main){
                                    captainDetails.value = snapshot.getValue(Captain::class.java)

                                }

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }
        }
    }

}