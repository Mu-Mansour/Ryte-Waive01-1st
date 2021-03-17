package com.example.Ui.HomeFragment

import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ryte.Logic.Captain
import com.example.ryte.Logic.PendingRideModule
import com.example.ryte.Network.NotificationData
import com.example.ryte.Network.PushNotification
import com.example.ryte.Network.RetrofitInstance
import com.example.ryte.Others.Utility
import com.example.ryte.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
    val theLiveDataOfCapsLocations =MutableLiveData<MutableList<MarkerOptions>>()
    var rideID:String?= null



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
    var cancelIsShown= false


    //for cap
    val theCapStatus: MutableLiveData<String> = MutableLiveData()
    val muCurrentRideIdForCaptain: MutableLiveData<String> = MutableLiveData()
    val theCapNewRide:MutableLiveData<Boolean> = MutableLiveData()
    val updatedEverything:MutableLiveData<Boolean> = MutableLiveData()
    val theCapRideIsCancelAbale:MutableLiveData<Boolean> = MutableLiveData()
    val isCstWithMe:MutableLiveData<Boolean> = MutableLiveData()
    var cstID:String?= null
    var theCstIDrawn=false
    var theCstLocationWithinRide:MutableLiveData<Location> = MutableLiveData()
    val theCstLatLng:MutableLiveData<LatLng> = MutableLiveData()
    var waitingServiceStarted=false
    var distanceServiceStarted=false





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
    fun getMyCurrentRideId()
    {
        viewModelScope.launch {
            FirebaseDatabase.getInstance().reference
                    .child("ConfirmedForCst")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists())
                            {
                                muCurrentRideId.value=snapshot.value.toString()+FirebaseAuth.getInstance().currentUser!!.uid
                                FirebaseDatabase.getInstance().reference
                                        .child("ConfirmedForCst")
                                        .child(FirebaseAuth.getInstance().currentUser!!.uid).removeEventListener(this)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
        }
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
            FirebaseDatabase.getInstance().reference.child("Customers").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Status").setValue("Ryte").addOnSuccessListener {
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
            FirebaseDatabase.getInstance().reference.child("Customers").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Status").setValue("WithCap")
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
            theDriver?.let {
                viewModelScope.launch(Dispatchers.IO) {
                    FirebaseDatabase.getInstance().reference.child("CustomersPending").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Cap is Coming").child(theDriver!!)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        theCaptainAnswer.value = snapshot.value.toString()
                                        theDriver?.let {
                                            FirebaseDatabase.getInstance().reference.child("CustomersPending")
                                                    .child(FirebaseAuth.getInstance().currentUser!!.uid).child("Cap is Coming").child(it).removeEventListener(this)
                                        }
                                        /*FirebaseDatabase.getInstance().reference.child("CustomersPending")
                                            .child(FirebaseAuth.getInstance().currentUser!!.uid).child("Cap is Coming").child(theDriver!!).removeEventListener(this)*/
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }

                            })

                }
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
                                viewModelScope.launch(Dispatchers.Main) {
                                    theCapStatus.value = snapshot.value.toString()
                                }

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
        }

        fun makeTheCapStatusOff(capId: String) {
            FirebaseDatabase.getInstance().reference.child("Captains").child(capId).child("Status").setValue("OFF").addOnSuccessListener {
           //     FirebaseDatabase.getInstance().reference.child("Cap Busy With Rides").child(capId).removeValue()

            }
        }

    fun updateMyStatusAfterArriving()
    {
        var cstidforCstNoti:String?=null
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseDatabase.getInstance().reference.child("Cap Busy With Rides").child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        cstidforCstNoti=snapshot.value.toString()
                        FirebaseDatabase.getInstance().reference.child("Rides")
                                .child("Pending").child(FirebaseAuth.getInstance().currentUser!!.uid+snapshot.value.toString()).child("CaptainArrived").setValue("yes").addOnSuccessListener {
                                    cstidforCstNoti?.let {
                                        viewModelScope.launch {
                                            sentDatNotificationtoThatCst(it)
                                            FirebaseDatabase.getInstance().reference.child("Captains")
                                                    .child(FirebaseAuth.getInstance().currentUser!!.uid).child("Status").setValue("Arrived")

                                        }
                                    }

                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        }
    }
    suspend fun sentDatNotificationtoThatCst(CstId: String) = CoroutineScope(Dispatchers.IO).launch {
        FirebaseDatabase.getInstance().reference.child("Users Tokens").child("Customers Tokens").child(CstId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val theCapToBeReached = snapshot.value.toString()
                    val notification = PushNotification(NotificationData("Ryter is Here ", "Please Reach Me"), theCapToBeReached)
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
        fun updateMyLocationAndStatusAsCaptain(myCity:String, myLocation:Location) {
            val theGeoLocation = GeoFire(FirebaseDatabase.getInstance().reference.child("Online Captains").child(myCity))
            theGeoLocation.setLocation(FirebaseAuth.getInstance().currentUser!!.uid, GeoLocation(myLocation.latitude, myLocation.longitude))
            FirebaseDatabase.getInstance().reference.child("Captains").child(FirebaseAuth.getInstance().currentUser!!.uid).child("zone").setValue(myCity).addOnSuccessListener {
                FirebaseDatabase.getInstance().reference.child("Captains").child(FirebaseAuth.getInstance().currentUser!!.uid).child("OnCity").setValue(myCity)
                FirebaseDatabase.getInstance().reference.child("Captains").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Status").setValue("ON")
            }
        }


    fun updateMyLocation(current:String,location: Location)
    {
        FirebaseDatabase.getInstance().reference.child("Captains").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("zone").addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists())
                        {
                            if (current==snapshot.value.toString())
                            {
                                val theGeoLocation2 = GeoFire( FirebaseDatabase.getInstance().reference.child("Online Captains").child(current))
                                theGeoLocation2.setLocation(FirebaseAuth.getInstance().currentUser!!.uid, GeoLocation(location.latitude, location.longitude))
                                FirebaseDatabase.getInstance().reference.child("Captains").child(FirebaseAuth.getInstance().currentUser!!.uid)
                                        .child("zone").removeEventListener(this)
                            }
                            else
                            {
                                val theGeoLocation = GeoFire(FirebaseDatabase.getInstance().reference.child("Online Captains").child(snapshot.value.toString()))
                                theGeoLocation.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
                                FirebaseDatabase.getInstance().reference.child("Captains").child(FirebaseAuth.getInstance().currentUser!!.uid).child("zone").setValue(current)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
    }


    fun getToCustomerToReach()=viewModelScope.launch(Dispatchers.IO){
        FirebaseDatabase.getInstance().reference.child("Cap Busy With Rides").child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(
                object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists())
                        {
                            cstID=snapshot.value.toString()
                            rideID=FirebaseAuth.getInstance().currentUser!!.uid+cstID
                            FirebaseDatabase.getInstance().reference.child("CustomersPending")
                                    .child(snapshot.value.toString()).child("Pending").addValueEventListener(object :ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists())
                                            {
                                                var theLat = snapshot.child("l").child("0").value.toString().toDouble()
                                                var theLong = snapshot.child("l").child("1").value.toString().toDouble()
                                                theCstLocationWithinRide.value=Location("").also {
                                                    it.latitude=theLat
                                                    it.longitude=theLong
                                                }
                                                theCstLatLng.value= LatLng(theLat,theLong)
                                            }
                                        }
                                        override fun onCancelled(error: DatabaseError) {
                                        }

                                    })

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                }
        )
    }


    fun makeTheAroundCaptains(city:String)
    {
        val thelocations= mutableListOf<MarkerOptions>()
        FirebaseDatabase.getInstance().reference.child("Online Captains").child(city).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChildren()) {
                    var maxCapstoBeShown = 0
                    thelocations.clear()
                    for (i in snapshot.children) {
                        if (i.key == FirebaseAuth.getInstance().currentUser!!.uid) {

                            var theLat = i.child("l").child("0").value.toString().toDouble()
                            var theLong = i.child("l").child("1").value.toString().toDouble()
                            var theMarker = MarkerOptions()
                                    .position(LatLng(theLat, theLong))
                                    .title("Me")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.finalmewheel))
                            thelocations.add(theMarker)
                        } else {


                            var theLat = i.child("l").child("0").value.toString().toDouble()
                            var theLong = i.child("l").child("1").value.toString().toDouble()
                            var thelocation = LatLng(theLat, theLong)
                            var theMarker = MarkerOptions()
                                    .position(thelocation)
                                    .title("Captain")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.lastcar))
                            thelocations.add(theMarker)

                            if (maxCapstoBeShown > 20) {
                                break
                            }
                            maxCapstoBeShown++

                        }
                    }
                    theLiveDataOfCapsLocations.value = thelocations


                } else {
                    thelocations.clear()
                    theLiveDataOfCapsLocations.value = thelocations
                }
                }

            override fun onCancelled(error: DatabaseError) {
            }

        }
)
    }
    fun listenToMyCancel(){
        FirebaseDatabase.getInstance().reference.child("CancelableRides").child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    viewModelScope.launch(Dispatchers.Main) {
                        theCapRideIsCancelAbale.value=true
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
    fun getMyCurrentRideIdForCap()
    {
        viewModelScope.launch {
            FirebaseDatabase.getInstance().reference
                    .child("Cap Busy With Rides")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists())
                            {
                                viewModelScope.launch(Dispatchers.Main) {
                                    muCurrentRideIdForCaptain.value=FirebaseAuth.getInstance().currentUser!!.uid+snapshot.value.toString()
                                    Utility.thePendingRideId=(FirebaseAuth.getInstance().currentUser!!.uid+snapshot.value.toString())
                                }
                                FirebaseDatabase.getInstance().reference
                                        .child("Cap Busy With Rides")
                                        .child(FirebaseAuth.getInstance().currentUser!!.uid).removeEventListener(this)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
        }
    }

    fun checktheCstIsWithMeToStartTheRide(rideid:String)
    {
        FirebaseDatabase.getInstance().reference.child("Rides")
                .child("Pending").child(rideid).child("Cstiswithme")
                .addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists())
                        {
                        var isHe=snapshot.value.toString().toBoolean()
                               isCstWithMe.value=isHe

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })

    }

fun makeMyStatusRiding()=viewModelScope.launch {
    FirebaseDatabase.getInstance().reference.child("Captains").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Status").setValue("Riding")
    }

    fun updateTheDistancAndWaitingTimeTotheRide(rideid: String) {

            if (Utility.distance > 0 || Utility.waitingTime > 0) {

                val distancePrice = Utility.distance.toString().toDouble().toString()

                val distanceinKilos = ((Utility.distance) / 1000).toString()

                val waitingPrice =((Utility.waitingTime.toString().toDouble() / 60) * 7).toString()

                val vat = (.14 * (distancePrice.toDouble() + waitingPrice.toDouble())).toString()

                val price =( (vat.toDouble() + waitingPrice.toDouble() + distancePrice.toDouble())).toString()



                FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(rideid).child("vat").setValue(vat).addOnSuccessListener { _ ->
                    FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(rideid).child("Distance").setValue(distanceinKilos).addOnSuccessListener { _ ->
                        FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(rideid).child("WaitingTime").setValue((Utility.waitingTime.toString())).addOnSuccessListener { _ ->
                            FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(rideid).child("Price").setValue(
                                price
                            ).addOnSuccessListener {
                                updatedEverything.value=true
                                viewModelScope.launch(Dispatchers.Main) {

                                }

                            }
                        }
                    }
                }
        }
        else
            {
                updatedEverything.value=false
                viewModelScope.launch(Dispatchers.Main) {


                }
            }

    }

}


