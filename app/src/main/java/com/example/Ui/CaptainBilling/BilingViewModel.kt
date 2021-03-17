package com.example.Ui.CaptainBilling

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class BilingViewMode @Inject constructor() : ViewModel() {

val muCurrentRideIdForCaptain:MutableLiveData<PendingRideModule> = MutableLiveData()
val updated:MutableLiveData<Boolean> = MutableLiveData()


    var cstOldCredit :Double?=null
    var capOldCredit :Double?=null
    var cstNewCredit :Double?=null
    var cashToBeCollected :Double?=null
    var theCapNewCreditToBeAdded :Double?=null
    var theCollectedAmount :Double?=null
    var didTheMath=true
    var capId:String?=null
    var cstId:String?=null




     fun doTheMathForCashThenCreditFirst(currentRide:PendingRideModule)
    {
        cstOldCredit?.let {
            when {
                it>= currentRide.Price!!.toDouble() -> {
                    cashToBeCollected=0.0
                    cstNewCredit= cstOldCredit!! - currentRide.Price!!.toDouble()
                    theCapNewCreditToBeAdded= currentRide.Price!!.toDouble()
                    capOldCredit = capOldCredit?.plus(theCapNewCreditToBeAdded!!)
                }
                it==0.0 -> {
                    cashToBeCollected= currentRide.Price!!.toDouble()
                    theCapNewCreditToBeAdded=0.0
                    cstNewCredit=0.0
                }
                else -> {
                    cashToBeCollected= currentRide.Price!!.toDouble()-cstOldCredit!!
                    theCapNewCreditToBeAdded=cstOldCredit!!
                    capOldCredit = capOldCredit?.plus(theCapNewCreditToBeAdded!!)
                    cstNewCredit=0.0

                }
            }
        }

    }
    fun  cashPayment(currentRide:PendingRideModule)=viewModelScope.launch {

        theCollectedAmount?.let {
            when
            {
                it> currentRide.Price!!.toDouble() ->{
                    /// take from the captain and add to the customer
                    cstNewCredit =it - currentRide!!.Price!!.toDouble()
                    cstNewCredit = cstNewCredit?.plus(cstOldCredit!!)

                    capOldCredit = (capOldCredit?.minus(it - currentRide.Price!!.toDouble()))
                    FirebaseDatabase.getInstance().reference.child("Customers").child(currentRide.Customer!!)
                        .child(  "Credit"  ).setValue(cstNewCredit!!.toString())
                    FirebaseDatabase.getInstance().reference.child("CaptainWallets").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue((capOldCredit!!-1.0).toString())

                }
                it== currentRide.Price!!.toDouble()->{
                    FirebaseDatabase.getInstance().reference.child("CaptainWallets").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue((capOldCredit!!-1.0).toString())

                }
            }
        }

    }
    fun dotheCompletionProcess(currentRide:PendingRideModule)=viewModelScope.launch {

       FirebaseDatabase.getInstance().reference.child("Rides").child("Completed").child(currentRide.Uid!!).setValue(
           currentRide
       ).addOnSuccessListener {
           FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(currentRide.Captain+currentRide.Customer).removeValue().addOnSuccessListener {
               FirebaseDatabase.getInstance().reference.child("Cap Busy With Rides").child(FirebaseAuth.getInstance().currentUser!!.uid).removeValue().addOnSuccessListener {
                   FirebaseDatabase.getInstance().reference.child("MyRides").child(FirebaseAuth.getInstance().currentUser!!.uid).child(
                       currentRide.Uid!!).setValue(currentRide).addOnSuccessListener {
                       FirebaseDatabase.getInstance().reference.child("MyRides").child(currentRide.Customer!!).child(
                           currentRide.Uid!!).setValue(currentRide).addOnSuccessListener {
                           FirebaseDatabase.getInstance().reference. child("CustomersPending").child(
                               currentRide.Customer!!).removeValue().addOnSuccessListener {
                               FirebaseDatabase.getInstance().reference.child("Captains").child(currentRide.Captain!!).child("Status").setValue("OFF").addOnSuccessListener {
                                   updated.value=true
                               }

                            }
                        }
                    }

                }
            }
        }
    }
    fun creditThenCashPayment(currentRide:PendingRideModule)=viewModelScope.launch {

        theCollectedAmount?.let {
            when {
                it > cashToBeCollected!!-> {
                    cstNewCredit = cstNewCredit?.plus((it- cashToBeCollected!!))
                    capOldCredit = capOldCredit?.minus(it- cashToBeCollected!!)
                    FirebaseDatabase.getInstance().reference.child("Customers").child(currentRide.Customer!!)
                        .child(  "Credit"  ).setValue((cstNewCredit!!.toString())).addOnSuccessListener {
                            FirebaseDatabase.getInstance().reference.child("CaptainWallets").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue((capOldCredit!!-1.0).toString())
                        }

                }
                it == cashToBeCollected!! -> {

                    FirebaseDatabase.getInstance().reference.child("CaptainWallets").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue((capOldCredit!!-1.0).toString())
                FirebaseDatabase.getInstance().reference.child("Customers").child(currentRide.Customer!!)
                        .child(  "Credit"  ).setValue((cstNewCredit!!.toString()))

                }

            }
        }

    }

    fun getMyCurrentRideIdForCap(rideId:String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference
                    .child("Rides")
                    .child("Pending").child(rideId).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists())
                            {

                                viewModelScope.launch(Dispatchers.Main) {
                                    muCurrentRideIdForCaptain.value= snapshot.getValue(PendingRideModule::class.java)

                                }

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
        }
    }
    fun initializeForCash(toBe:Double)
    {
        cstNewCredit =null
        cashToBeCollected=toBe
        theCapNewCreditToBeAdded=null
        didTheMath=true
    }

    fun getTheCstOldCredit(cstIdForCredit:String)
    {

        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("Customers").child(cstIdForCredit!!)
                    .child(  "Credit"  ).addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists())
                        {
                            cstOldCredit=snapshot.value.toString().toDouble()
                            FirebaseDatabase.getInstance().reference.child("Customers").child(cstId!!)
                                    .child(  "Credit"  ).removeEventListener(this)
                        }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
        }

    }
    fun getTheCapCredit()
    {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("CaptainWallets").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists())
                        {
                            capOldCredit=snapshot.value.toString().toDouble()
                            FirebaseDatabase.getInstance().reference.child("CaptainWallets").child(cstId!!)
                                .child(  FirebaseAuth.getInstance().currentUser!!.uid  ).removeEventListener(this)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }

    }
}