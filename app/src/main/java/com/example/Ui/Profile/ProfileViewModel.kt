package com.example.Ui.Profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ryte.Logic.Captain
import com.example.ryte.Logic.Customer
import com.example.ryte.Logic.PendingRideModule
import com.firebase.geofire.GeoFire
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
    val theCapNewRide:MutableLiveData<Boolean> = MutableLiveData()
    var cstID:String?=null





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

    fun listenToMyRides()=FirebaseDatabase.getInstance().reference. child("Pending For Caps")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).child("TheCStId").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        cstID=snapshot.value.toString()
                        theCapNewRide.value= true

                    }
                    else
                    {
                        /* theCapNewRide.value= false
                         //cstID=null*/
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })


    fun takeTheRide(){
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference
                    .child("Pending For Caps").child(FirebaseAuth.getInstance().currentUser!!.uid).child("TheCStId").removeValue().addOnSuccessListener {
                        FirebaseDatabase.getInstance().reference.child("CustomersPending").
                        child(cstID!!).child("Cap is Coming").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue("yes").addOnSuccessListener {
                            FirebaseDatabase.getInstance().reference.child("Captains").child(FirebaseAuth.getInstance()
                                    .currentUser!!.uid).child("OnCity").addValueEventListener(
                                    object :ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if(snapshot.exists())
                                            {
                                                var theOnCity=snapshot.value.toString()
                                                val theGeoLocation = GeoFire(FirebaseDatabase.getInstance().reference.child("Online Captains").child(theOnCity))
                                                theGeoLocation.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
                                                FirebaseDatabase.getInstance().reference.child("Captains").child(FirebaseAuth.getInstance().currentUser!!.uid).child("OnCity")
                                                        .addValueEventListener(object : ValueEventListener {
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                val theoncity = snapshot.value.toString()
                                                                if (theOnCity != theoncity) {
                                                                    val theGeoLocationofOn = GeoFire(FirebaseDatabase.getInstance().reference.child("Online Captains").child(theoncity))
                                                                    theGeoLocationofOn.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
                                                                }
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {
                                                            }

                                                        })                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                        }

                                    }
                                    )
                        }
                        FirebaseDatabase.getInstance().reference.child("Captains").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Status").setValue("Busy").addOnSuccessListener {
                            FirebaseDatabase.getInstance().reference.child("Cap Busy With Rides").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(cstID).addOnSuccessListener {

                            }
                        }
                    }

        }
    }

}