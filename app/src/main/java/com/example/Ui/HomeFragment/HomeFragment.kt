package com.example.Ui.HomeFragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.example.Ui.CancelCurrentRide.CancelDialouge
import com.example.Ui.customerCurrentCaptainDetails.CurrentCaptainDetails
import com.example.ryte.Others.Utility
import com.example.ryte.R
import com.example.ryte.Services.CalculatingDistanceService
import com.example.ryte.Services.CalculatingWaitingTimeService
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
//general
    private lateinit var theMap: GoogleMap
    private val arguments: HomeFragmentArgs by navArgs()
    private val theViewModel:HomeViewModel by viewModels()
    lateinit var fusedLocation: FusedLocationProviderClient
    lateinit var theProgress: ProgressDialog
    lateinit var theProgress2: ProgressDialog



    //caps


    //csts

   private val locationForUpdates =LocationRequest().apply {
       priority = LocationRequest.PRIORITY_HIGH_ACCURACY
       fastestInterval= 3000
       interval= 3000
   }
    private var theMarke: Marker? = null
    private var theMarke2: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocation=LocationServices.getFusedLocationProviderClient(requireContext())

        if (arguments.UserType=="Cst")
        {
            lifecycleScope.launch {
                theViewModel.listenToCstStatus()
            }
        }
        else
        {
            lifecycleScope.launch {
                theViewModel.listenToCapStatus()
                theViewModel.getToCustomerToReach()
            }
        }
    }


    @SuppressLint("UseCompatLoadingForColorStateLists", "MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView2.onCreate(savedInstanceState)
        mapView2.getMapAsync { it ->
            theMap = it
            theMap.uiSettings.isZoomControlsEnabled = true
           theMap.uiSettings.isMyLocationButtonEnabled = true
        }

        userProfile.load(arguments.image){
            crossfade(true)
            transformations(CircleCropTransformation())
            scale(Scale.FILL)
        }
        userProfile.setOnClickListener {

                theViewModel.theCstLatLng.removeObservers(this)
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment2(arguments.UserType))



        }
         theProgress= ProgressDialog(context)
        theProgress2= ProgressDialog(context)
        theProgress2.setMessage("Updating your Location..")
        theProgress2.setCancelable(false)
        theProgress2.setCanceledOnTouchOutside(false)
        theProgress2.show()

        if (arguments.UserType=="Cst")
        {
            theViewModel.theCstStatus.observe(viewLifecycleOwner, { cst ->
                cst?.let { it ->
                    when (it) {
                        "Ryte" -> {
                            cancelTheRide.isVisible = false
                            doJobBtn.apply {
                                //  backgroundTintList = resources.getColorStateList(R.color.white)
                                setImageResource(R.drawable.ic_baseline_maps_ugc_24)
                                setOnClickListener {
                                    lifecycleScope.launch {
                                        findCloseCapsForCst()
                                    }
                                }
                            }

                        }
                        "Waiting" -> {

                            doJobBtn.apply {
                                visibility = View.VISIBLE
                                //          backgroundTintList = resources.getColorStateList(R.color.white)
                                setImageResource(R.drawable.ic_baseline_person_24)
                                setOnClickListener {
                                    lifecycleScope.launch {
                                        showMyCaptainDetails(parentFragmentManager)
                                    }

                                }
                            }
                            cancelTheRide.apply {
                                visibility = View.VISIBLE
                                setOnClickListener {
                                    theViewModel.getMyCurrentRideId()
                                    theViewModel.muCurrentRideId.observe(viewLifecycleOwner, { id1 ->
                                        id1?.let {
                                          //  Toast.makeText(requireContext(), id1, Toast.LENGTH_SHORT).show()
                                            if (!theViewModel.cancelIsShown)
                                            {
                                                cancelTheRide(parentFragmentManager)
                                            }

                                       //     theViewModel.muCurrentRideId.value=null
                                        } /*?:Toast.makeText(requireContext(), "Please Confirm Your Captain Details First ", Toast.LENGTH_SHORT).show()*/
                                    })



                                }
                            }

                        }
                        "Riding" -> {
                            cancelTheRide.isVisible = false
                            doJobBtn.apply {
                                visibility = View.VISIBLE
                                setImageResource(R.drawable.ic_baseline_check_24)
                                setOnClickListener {
                                    lifecycleScope.launch {
                                        theViewModel.makeMeWithCaptain()
                                        val theIntentForTracking = Intent(context, CalculatingDistanceService::class.java).apply {
                                            action = Utility.startRide
                                        }
                                        val thewaitingIntent = Intent(requireContext(), CalculatingWaitingTimeService::class.java)
                                        requireActivity().stopService(thewaitingIntent)
                                        ContextCompat.startForegroundService(context, theIntentForTracking)
                                    }

                                }
                            }


                        }
                        "WithCap" -> {
                            cancelTheRide.isVisible = false
                            doJobBtn.apply {
                                visibility = View.VISIBLE
                                setImageResource(R.drawable.ic_baseline_location_on_24)
                                setOnClickListener {
                                    lifecycleScope.launch {
                                        AlertDialog.Builder(requireContext()).apply {
                                            setMessage("You Are about Completing Your Ride \nPlease Confirm The Ride payment Bill with your Captain ")
                                            setTitle("Completion Dialogue  ")
                                            setPositiveButton(" Confirm ") { _, _ ->
                                                val toStopAllocating = Intent(requireContext(), CalculatingDistanceService::class.java)
                                                requireActivity().stopService(toStopAllocating)
                                                lifecycleScope.launch(Dispatchers.IO) {
                                                    theViewModel.makeMeRyte()
                                                }

                                                Toast.makeText(requireContext(), "Thank You For Ryting ", Toast.LENGTH_SHORT).show()

                                            }
                                            setNegativeButton("Cancel ") { _, _ ->
                                                Toast.makeText(requireContext(), "Cancelled...Ryte Still tracking Your Location", Toast.LENGTH_SHORT).show()
                                            }
                                        }.show()
                                    }

                                }
                            }

                        }
                    }
                }
            })
        }
        else
        {

            theViewModel.theCapNewRide.observe(viewLifecycleOwner, {
                it?.let {

                    if (it) {
                        theViewModel.theCapNewRide.value=false
                        val theDialog =  AlertDialog.Builder(requireContext())
                        theDialog .setMessage("You  Have a New Ride")
                                .setTitle("New Ride ").setPositiveButton("Accept") { _, _ ->
                                    lifecycleScope.launch {

                                    }

                                }.setNegativeButton("Decline") { _, _ ->


                                }.show()


                    } else {
                        //for the dialouge

                    }
                }
            })
            theViewModel.theCapStatus.observe(viewLifecycleOwner,{cap->
                cap?.let {
                    when(it)
                    {
                        "Busy" -> {
                            doJobBtn.setImageResource(R.drawable.ic_baseline_location_on_24)
                            theViewModel.getToCustomerToReach()
                            theViewModel.theCstLocationWithinRide.observe(viewLifecycleOwner, { location ->
                                location?.let {
                                    val theMarker = MarkerOptions()
                                            .position(LatLng(location.latitude, location.longitude))
                                            .title("Captain")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.sycee))
                                    theMap.addMarker(theMarker)
                                    fusedLocation.lastLocation.addOnSuccessListener { theCurrentLocation ->

                                        doJobBtn.setOnClickListener {
                                            if (theCurrentLocation.distanceTo(location) <= 50.0) {
                                                Toast.makeText(requireContext(), "You Reached Your Destination", Toast.LENGTH_SHORT).show()
                                                theViewModel.updateMyStatusAfterArriving()

                                            } else {
                                                Toast.makeText(requireContext(), "Please Reach Your Destination ", Toast.LENGTH_SHORT).show()

                                            }
                                        }

                                    }

                                }
                            })
                            doJobBtn.apply {
                                setImageResource(R.drawable.ic_baseline_location_on_24)
                                visibility = View.VISIBLE
                            }
                        }
                        "ON"->{
                            doJobBtn.visibility=View.GONE
                            statusBTN.apply {
                                visibility=View.VISIBLE
                               background=resources.getDrawable(R.drawable.radious4)
                                text="ON"
                                setOnClickListener {
                                    fusedLocation.lastLocation.addOnSuccessListener {
                                        val theGeoCoder = Geocoder(requireContext(), Locale.getDefault())
                                        val adreess = theGeoCoder.getFromLocation(it.latitude, it.longitude, 1)
                                        theViewModel.theCity = adreess[0].locality.toString()
                                        theViewModel.makeTheCapStatusOff(FirebaseAuth.getInstance().currentUser!!.uid)
                                        theViewModel.makeTheCapOfflilne(FirebaseAuth.getInstance().currentUser!!.uid)
                                    }
                                }
                            }
                            RefreshMyLocation.apply {
                                visibility=View.VISIBLE
                                setOnClickListener {
                                    refreshMyLocation()
                                }
                            }

                        }
                        "Arrived"->{


                        }
                        "Riding"->{

                        }
                        "OFF"->{
                            theViewModel.makeTheCapStatusOff(FirebaseAuth.getInstance().currentUser!!.uid)
                            doJobBtn.isVisible=false
                            RefreshMyLocation.visibility=View.GONE
                            statusBTN.apply {
                            visibility=View.VISIBLE
                                background=resources.getDrawable(R.drawable.radious5)
                            text="OFF"
                            setOnClickListener {
                                fusedLocation.lastLocation.addOnSuccessListener {
                                    val theGeoCoder = Geocoder(requireContext(), Locale.getDefault())
                                    val adreess = theGeoCoder.getFromLocation(it.latitude, it.longitude, 1)
                                    theViewModel.theCity = adreess[0].locality.toString()
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        makeTheCaptainOnline()
                                    }

                                }


                            }

                        }
                        }
                    }
                }
            })
        }





       val  theLocationCallBcak= object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                if (arguments.UserType != "Cst") {
                    isLocationEnabled(requireContext())
                    p0?.let {
                        theMarke?.remove()
                        theMarke = theMap.addMarker(MarkerOptions()
                                .position(LatLng(p0!!.lastLocation.latitude, p0.lastLocation.longitude))
                                .title("Captain")
                                .rotation(p0.lastLocation.bearing)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carforcaptainmoving)))
                        theMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p0.lastLocation.latitude, p0.lastLocation.longitude), 18f))
                        theProgress2.dismiss()


                    }

                } else {
                    isLocationEnabled(requireContext())
                    p0?.let {
                        theMarke?.remove()
                        theMarke = theMap.addMarker(MarkerOptions()
                                .position(LatLng(p0.lastLocation.latitude, p0.lastLocation.longitude))
                                .title("Customer")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.customer)))
                        theMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p0.lastLocation.latitude, p0.lastLocation.longitude), 18f))
                        theProgress2.dismiss()



                    }
                }
            }
        }




        theViewModel.isLocationEnabled.observe(viewLifecycleOwner, {
            it?.let {
                if (!it) {
                    AlertDialog.Builder(requireContext())
                        .setMessage("Please Enable Location Services").setCancelable(false)
                        .setPositiveButton("Enable") { _: DialogInterface, _: Int ->
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }.show()
                }


            }
        })
        loopTheLocationWhileOn(theLocationCallBcak)

    }

    private fun cancelTheRide(parentFragmentManager: FragmentManager) {
        theViewModel.cancelIsShown=false
             theViewModel.muCurrentRideId.observe(viewLifecycleOwner, {
            it?.let {
                theViewModel.getTheCurrentRideDetails(it)
                theViewModel.muCurrentRideDetails.observe(viewLifecycleOwner, { ride ->
                    ride?.let { theRide ->
                        val theCancelDialogue = CancelDialouge(theRide)
                        theCancelDialogue.show(parentFragmentManager, null)
                        theViewModel.cancelIsShown=true
                        theViewModel.muCurrentRideDetails.value=null
                        theViewModel.muCurrentRideId.value=null
                    }
                })

            }
        })

    }

    private fun showMyCaptainDetails(parentFragmentManager: FragmentManager) {


        lifecycleScope.launch(Dispatchers.IO) {
            theViewModel.getMyCurrentCaptain()
        }
        theViewModel.myCaptainId.observe(viewLifecycleOwner,{it->
            it?.let {
                lifecycleScope.launch {
                    theViewModel.getMyCaptainDetails(it)
                    //theViewModel.muCurrentRideId.value=it+FirebaseAuth.getInstance().currentUser!!.uid
                    theViewModel.myCaptainDetails.observe(viewLifecycleOwner,{cap->
                        cap?.let {theCap->
                            CurrentCaptainDetails(theCap,it).show(parentFragmentManager,null)
                            theViewModel.myCaptainDetails.value=null

                        }
                    })
                }

            }
        })


    }

    private fun isLocationEnabled(context: Context) {
        theViewModel.isLocationEnabled.value= LocationManagerCompat.isLocationEnabled( context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
    }
    override fun onPause() {
        super.onPause()
        mapView2?.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView2?.onResume()
        showArounCaptainsAndStoreem()
        theViewModel.theLiveDataOfCapsLocations.observe(viewLifecycleOwner, { theLOcationList ->
            if (theLOcationList.size != 0) {
                theMap.clear()

                for (i in theLOcationList) {
                    theMap.addMarker(i)
                }
            } else {
                theMap.clear()
                theViewModel.getToCustomerToReach()
            }

        })
        viewLifecycleOwner.let {
            theViewModel.theCstLocationWithinRide.observe(it, { location ->
                location?.let {
                    if (!theViewModel.theCstIDrawn)
                    {
                        theMarke2?.remove()
                        theMarke2= theMap.addMarker(MarkerOptions()
                                .position(LatLng(location.latitude, location.longitude))
                                .title("Captain")
                            //    .rotation(p0.lastLocation.bearing)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.sycee)))
                       // theMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p0.lastLocation.latitude, p0.lastLocation.longitude), 18f))
                    //    theProgress2.dismiss()
                        theViewModel.theCstIDrawn=true
                    }


                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
       mapView2?.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView2?.onDestroy()

    }


    override fun onLowMemory() {
        super.onLowMemory()
        mapView2?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView2?.onSaveInstanceState(outState)
    }
    @SuppressLint("MissingPermission")
    fun loopTheLocationWhileOn(ctb: LocationCallback) {
        fusedLocation.requestLocationUpdates(locationForUpdates, ctb,
                Looper.myLooper())
    }


    //funs for cst


    @SuppressLint("MissingPermission")
    fun findCloseCapsForCst()
    {
        theProgress.setMessage("Searching..")
        theProgress.setCancelable(false)
        theProgress.setCanceledOnTouchOutside(false)
        theProgress.show()
        fusedLocation.lastLocation.addOnSuccessListener {
            val theGeoCoder = Geocoder(requireContext(), Locale.getDefault())
           val adreess:List<Address> = theGeoCoder.getFromLocation(it.latitude,it.longitude,1)
            theViewModel.makeTheLocationToSatrtSearchingForCaps(adreess[0].locality.toString(), GeoLocation(it.latitude,it.longitude))
           val theGeoLocationRef = GeoFire( FirebaseDatabase.getInstance().reference.child("Online Captains").child(adreess[0].locality.toString()))
            lifecycleScope.launch(Dispatchers.IO) {
                findCaptainsWithRecursion(theGeoLocationRef,theViewModel.geoRefrence!!,requireContext())
            }
        }

    }
    suspend fun findCaptainsWithRecursion(locationref:GeoFire, geoLocation: GeoLocation, context: Context )
    {

        val theGeoQuery = locationref.queryAtLocation(geoLocation, theViewModel.theDistance)
        theGeoQuery.removeAllListeners()
        theGeoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                if (!theViewModel.isFound )
                {
                    theViewModel. isFound = true
                    theViewModel.theDriver = key!!
                    lifecycleScope.launch(Dispatchers.IO)  {
                        theViewModel.theDriver?.let {
                            theViewModel.makeTheCapOfflilne( it)
                            theViewModel.makeTheCapStatusOff(it)
                            theViewModel.sentDatNotificationtoThatCap( it)
                        }
                    }
                    theViewModel.createRefrenceForMyPendingStatus()
                        lifecycleScope.launch(Dispatchers.IO)
                        {
                            delay(10000)
                            theViewModel.checkIfCapAccepted()
                            delay(5000)
                            lifecycleScope.launch(Dispatchers.Main){
                                theViewModel.theCaptainAnswer.observe(viewLifecycleOwner,{
                                    it?.let {
                                        if (it=="yes") {
                                            theViewModel.updateAfterCaptainAcceptance()
                                            theViewModel.rideID=theViewModel.theDriver+FirebaseAuth.getInstance().currentUser!!.uid
                                            Utility.thePendingRideId=theViewModel.theDriver+FirebaseAuth.getInstance().currentUser!!.uid
                                            val theWaitingIntent= Intent(context, CalculatingWaitingTimeService::class.java).apply {
                                                action= Utility.startWaitingService

                                            }
                                            ContextCompat.startForegroundService(context, theWaitingIntent)
                                            theGeoQuery.removeAllListeners()
                                            theProgress.dismiss()
                                        }
                                        else {
                                            theViewModel.updateAfterCaptainRejection()
                                            lifecycleScope.launch(Dispatchers.IO) {
                                                findCaptainsWithRecursion(locationref,theViewModel.geoRefrence!!,requireContext())
                                            }
                                        }
                                    }
                                })
                            }
                        }



                    theGeoQuery.removeAllListeners()

                }

                return
            }
            override fun onKeyExited(key: String?) {
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {

            }

            override fun onGeoQueryReady() {
                if (!theViewModel.isFound &&theViewModel.theDistance<=theViewModel.theRadiouslLimit)
                {
                    theGeoQuery.removeAllListeners()
                    theViewModel.theDistance++
                    CoroutineScope(Dispatchers.IO).launch {
                        findCaptainsWithRecursion(locationref,geoLocation,context)
                    }
                }
                else if (theViewModel.theDistance> theViewModel.theRadiouslLimit)
                {
                    theProgress.dismiss()
                    theViewModel.couldntFindCaptainsNearBy()
                    Toast.makeText(context, "we couldn't Find Captains here", Toast.LENGTH_SHORT).show()
                    theGeoQuery.removeAllListeners()
                    theViewModel.resetBeforeSearching()
                    theViewModel.theCaptainAnswer.value=null
                    return
                }

            }

            override fun onGeoQueryError(error: DatabaseError?) {
            }
        })


    }

    @SuppressLint("MissingPermission")
    fun refreshMyLocation() {
        fusedLocation.lastLocation.addOnSuccessListener {
            val theGeoCoder = Geocoder(requireContext(), Locale.getDefault())
            val adreess = theGeoCoder.getFromLocation(it.latitude, it.longitude, 1)
            val theCity = adreess[0].locality.toString()
           theViewModel.updateMyLocation(theCity,it)

        }





    }


    @SuppressLint("MissingPermission")
    fun makeTheCaptainOnline(){
        fusedLocation.lastLocation.addOnSuccessListener {
            it?.let {
                val theGeoCoder = Geocoder(requireContext(), Locale.getDefault())
                val adreess = theGeoCoder.getFromLocation(it.latitude, it.longitude, 1)
                val theCity = adreess[0]?.locality
                if (theCity != null) {
                    theViewModel.updateMyLocationAndStatusAsCaptain(theCity,it)
                }
            }



        }


    }
    @SuppressLint("MissingPermission", "VisibleForTests")
    fun showArounCaptainsAndStoreem()
    {

lifecycleScope.launch {
    fusedLocation.lastLocation.addOnSuccessListener { thisLocation ->
        if (thisLocation!= null){
            val theGeoCoder = Geocoder(context, Locale.getDefault())
            val adreess: MutableList<Address> ?= theGeoCoder.getFromLocation(thisLocation.latitude, thisLocation.longitude, 1)
            if (adreess?.get(0)?.locality!= null) {
                theViewModel.makeTheAroundCaptains(adreess[0].locality)
            }
        }
    }
}


    }

    }

