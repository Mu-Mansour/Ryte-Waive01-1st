package com.example.Ui.HomeFragment

import android.annotation.SuppressLint
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ryte.Logic.Captain
import com.example.ryte.Logic.PendingRideModule
import com.example.ryte.Network.NotificationData
import com.example.ryte.Network.PushNotification
import com.example.ryte.Network.RetrofitInstance
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

@HiltViewModel
class HomeViewModel @Inject constructor() :ViewModel() {


    //for both
    val isLocationEnabled: MutableLiveData<Boolean> = MutableLiveData()


    //for cst
    val theCstStatus: MutableLiveData<String> = MutableLiveData()
    val myCaptainId: MutableLiveData<String> = MutableLiveData()
    val muCurrentRideId: MutableLiveData<String> = MutableLiveData()
    val muCurrentRideDetails: MutableLiveData<PendingRideModule> = MutableLiveData()
    val myCaptainDetails: MutableLiveData<Captain> = MutableLiveData()
    var theDriver: String? = null
    var theCity: String? = null
    var theCaptainAnswer: MutableLiveData<String> = MutableLiveData()
    var theDistance = 1.0
    var geoRefrence: GeoLocation? = null

    var theRadiouslLimit = 5.0
    var theDriverisfound = false
    var isFound = false


    //for cap
    val theCapStatus: MutableLiveData<String> = MutableLiveData()


    // funs for cst

    fun listenToCstStatus() {
        FirebaseDatabase.getInstance().reference.child("Customers").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Status").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            theCstStatus.value = snapshot.value.toString()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
    }

    fun getMyCurrentCaptain() {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("ConfirmedForCst").child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    myCaptainId.value = snapshot.value.toString()
                                }
                                FirebaseDatabase.getInstance().reference.child("ConfirmedForCst").child(FirebaseAuth.getInstance().currentUser!!.uid).removeEventListener(this)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
        }
    }

    fun getMyCaptainDetails(capId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("Captains").child(capId)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    myCaptainDetails.value = snapshot.getValue(Captain::class.java)
                                }
                                FirebaseDatabase.getInstance().reference.child("Captains").child(capId).removeEventListener(this)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
        }
    }

    fun getTheCurrentRideDetails(rideId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(rideId)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    muCurrentRideDetails.value = snapshot.getValue(PendingRideModule::class.java)
                                }
                                FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(rideId).removeEventListener(this)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
        }
    }

    fun makeMeRyte() {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("Customers").child("Status").setValue("Ryte").addOnSuccessListener {
                FirebaseDatabase.getInstance().reference.child("ConfirmedForCst")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid).removeValue().addOnSuccessListener {
                            resetBeforeSearching()
                        }
            }
        }
    }

    fun resetBeforeSearching() {
        theDistance = 1.0
        theDriverisfound= false
        isFound = false
        theDriver= null
    }


    fun makeTheLocationToSatrtSearchingForCaps(city: String?, theGeoLocation: GeoLocation) {
        geoRefrence = theGeoLocation
        city?.let {
            theCity = it
            geoRefrence?.let { loc ->
                GeoFire(FirebaseDatabase.getInstance().reference.child("CustomersPending").child(FirebaseAuth.getInstance().currentUser!!.uid)).setLocation("Pending", loc)
            }
            GeoFire(FirebaseDatabase.getInstance().reference.child(it))

        }
    }
    fun couldntFindCaptainsNearBy()
    {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("CustomersPending").child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("Cap is Coming").removeValue().addOnSuccessListener {
                        FirebaseDatabase.getInstance().reference.child("CustomersPending").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Pending").removeValue()
                    }
        }
    }

    fun makeTheCapOfflilne(capId: String) {

        val theGeoLocation = GeoFire(FirebaseDatabase.getInstance().reference.child("Online Captains").child(theCity!!))
        theGeoLocation.removeLocation(capId)
        FirebaseDatabase.getInstance().reference.child("Captains").child(capId).child("OnCity")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val theoncity = snapshot.value.toString()
                        if (theCity != theoncity) {
                            val theGeoLocationofOn = GeoFire(FirebaseDatabase.getInstance().reference.child("Online Captains").child(theoncity))
                            theGeoLocationofOn.removeLocation(capId)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
    }


    fun makeMeWithCaptain() {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("Customers").child("Status").setValue("WithCap")
        }
    }


    suspend fun sentDatNotificationtoThatCap(capId: String) = CoroutineScope(Dispatchers.IO).launch {
        FirebaseDatabase.getInstance().reference.child("Users Tokens").child("Captains Tokens").child(capId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val theCapToBeReached = snapshot.value.toString()
                    val notification = PushNotification(NotificationData("New Ride ", "Check Your Application"), theCapToBeReached)
                    viewModelScope.launch(Dispatchers.IO) {
                        val response = RetrofitInstance.api.postNotification(notification)
                        if (response.isSuccessful) {
                            // more logic
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    fun createRefrenceForMyPendingStatus() = viewModelScope.launch(Dispatchers.IO) {
        FirebaseDatabase.getInstance().reference.child("CustomersPending").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Cap is Coming").child(theDriver!!).setValue("notyet").addOnSuccessListener {
                    FirebaseDatabase.getInstance().reference.child("Pending For Caps").child(theDriver!!).child("TheCStId").setValue(FirebaseAuth.getInstance().currentUser!!.uid)

                }
    }
        fun checkIfCapAccepted() {
            viewModelScope.launch(Dispatchers.IO) {


                FirebaseDatabase.getInstance().reference.child("CustomersPending").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Cap is Coming").child(theDriver!!)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    theCaptainAnswer.value = snapshot.value.toString()
                                    FirebaseDatabase.getInstance().reference.child("CustomersPending")
                                            .child(FirebaseAuth.getInstance().currentUser!!.uid).child("Cap is Coming").child(theDriver!!).removeEventListener(this)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })

        }
    }
    fun updateAfterCaptainAcceptance() {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("ConfirmedForCst").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(theDriver!!).addOnSuccessListener {
                FirebaseDatabase.getInstance().reference.child("CustomersPending").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Cap is Coming").removeValue().addOnSuccessListener {
                    FirebaseDatabase.getInstance().reference.child("Customers").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Status").setValue("Waiting").addOnSuccessListener {
                        createThePendingRideDetails(theDriver!!)
                    }
                }
            }

        }
    }
    fun updateAfterCaptainRejection()
    {
        makeTheCapStatusOff(theDriver!!)
        FirebaseDatabase.getInstance().reference.child("Pending For Caps").child(theDriver!!).removeValue()
        resetBeforeSearching()
    }
    @SuppressLint("SimpleDateFormat")
    fun createThePendingRideDetails(capId:String) {

        val theRideDetails = HashMap<String, Any>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")
            theRideDetails["StartingTime"]=  current.format(formatter)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("MMM dd yyyy HH:mma")
            theRideDetails["StartingTime"]=formatter.format(date)
        }
        theRideDetails["CancelableWithFees"]="true"
        theRideDetails["Cstiswithme"]="false"
        theRideDetails["Customer"]= FirebaseAuth.getInstance().currentUser!!.uid
        theRideDetails["Captain"]= capId
        theRideDetails["Uid"]= FirebaseDatabase.getInstance().reference.push().key.toString()
        theRideDetails["CaptainArrived"]= "Nope"
        theRideDetails["Distance"]= "0.0"
        theRideDetails["WaitingTime"]= "0"
        theRideDetails["Price"]= "0.0"
        theRideDetails["vat"]= "0.0"
       FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(capId+FirebaseAuth.getInstance().currentUser!!.uid).updateChildren(theRideDetails)

    }

        // funs for captain


        fun listenToCapStatus() {
            FirebaseDatabase.getInstance().reference.child("Captains").child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("Status").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                theCapStatus.value = snapshot.value.toString()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
        }

        fun makeTheCapStatusOff(capId: String) {
            FirebaseDatabase.getInstance().reference.child("Captains").child(capId).child("Status").setValue("OFF").addOnSuccessListener {

            }
        }


    }


