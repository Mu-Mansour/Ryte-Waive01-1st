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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
//general
    private lateinit var theMap: GoogleMap
    private val arguments: HomeFragmentArgs by navArgs()
    private val theViewModel:HomeViewModel by viewModels()
    lateinit var fusedLocation: FusedLocationProviderClient
    lateinit var theProgress: ProgressDialog

    //caps


    //csts

   private val locationForUpdates =LocationRequest().apply {
       priority = LocationRequest.PRIORITY_HIGH_ACCURACY
       fastestInterval= 3000
       interval= 3000
   }
    private var theMarke: Marker? = null

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
            }
        }
    }


    @SuppressLint("UseCompatLoadingForColorStateLists")
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
         theProgress= ProgressDialog(context)
        theProgress.setMessage("Updating your Location..")
        theProgress.setCancelable(false)
        theProgress.setCanceledOnTouchOutside(false)
        theProgress.show()

        if (arguments.UserType=="Cst")
        {
            theViewModel.theCstStatus.observe(viewLifecycleOwner, { cst ->
                cst?.let {
                    when (it) {
                        "Ryte" -> {

                            doJobBtn.apply {
                                backgroundTintList = resources.getColorStateList(R.color.white)
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
                                backgroundTintList = resources.getColorStateList(R.color.white)
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
                                    cancelTheRide(parentFragmentManager)
                                }
                            }

                        }
                        "Riding" -> {
                            doJobBtn.apply {
                                visibility = View.VISIBLE
                                backgroundTintList = resources.getColorStateList(R.color.white)
                                setImageResource(R.drawable.ic_baseline_check_24)
                                setOnClickListener {
                                    lifecycleScope.launch {
                                        theViewModel.makeMeWithCaptain()
                                    }

                                }
                            }


                        }
                        "WithCap" -> {
                            doJobBtn.apply {
                                visibility = View.VISIBLE
                                backgroundTintList = resources.getColorStateList(R.color.white)
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
            theViewModel.theCapStatus.observe(viewLifecycleOwner,{cap->
                cap?.let {
                    when(it)
                    {
                        "Busy"->{

                        }
                        "ON"->{

                        }
                        "Arrived"->{

                        }
                        "Riding"->{

                        }
                        "OFF"->{

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
                        theMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p0.lastLocation.latitude, p0.lastLocation.longitude), 12f))
                        theProgress.dismiss()

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
                        theProgress.dismiss()


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
        theViewModel.muCurrentRideId.observe(viewLifecycleOwner,{
            it?.let {
                theViewModel.getTheCurrentRideDetails(it)
                theViewModel.muCurrentRideDetails.observe(viewLifecycleOwner,{ride->
                    ride?.let { theRide->
                     val theCancelDialogue=CancelDialouge(theRide)
                        theCancelDialogue.show(parentFragmentManager,null)
                    }
                })
            }
        })

    }

    private fun showMyCaptainDetails(parentFragmentManager: FragmentManager) {

        theViewModel.getMyCurrentCaptain()
        theViewModel.myCaptainId.observe(viewLifecycleOwner,{it->
            it?.let {
                theViewModel.getMyCaptainDetails(it)
                theViewModel.myCaptainDetails.observe(viewLifecycleOwner,{cap->
                    cap?.let {theCap->
                         CurrentCaptainDetails(theCap,it).show(parentFragmentManager,null)
                    }
                })
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
           // Toast.makeText(requireContext(), "${adreess}", Toast.LENGTH_SHORT).show()
            theViewModel.makeTheLocationToSatrtSearchingForCaps(adreess[0]?.locality.toString(), GeoLocation(it.latitude,it.longitude))
           val theGeoLocationRef = GeoFire( FirebaseDatabase.getInstance().reference.child("Online Captains").child(adreess[0]?.locality.toString()))
            lifecycleScope.launch(Dispatchers.IO) {
                findCaptainsWithRecursion(theGeoLocationRef,theViewModel.geoRefrence!!,requireContext(),theProgress)
            }
        }

    }
    suspend fun findCaptainsWithRecursion(locationref:GeoFire, geoLocation: GeoLocation, context: Context, dialog: AlertDialog)
    {

        val theGeoQuery = locationref.queryAtLocation(geoLocation, theViewModel.theDistance)
        theGeoQuery.removeAllListeners()
        theGeoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                if (!theViewModel.isFound )
                {
                    theViewModel.theDriver = key!!
                    lifecycleScope.launch(Dispatchers.IO)  {
                        theViewModel.theDriver?.let {
                            theViewModel.makeTheCapOfflilne( it)
                            theViewModel.makeTheCapStatusOff(it)
                            theViewModel.sentDatNotificationtoThatCap( it)
                        }
                    }
                    theViewModel.createRefrenceForMyPendingStatus()
                    lifecycleScope.launch(Dispatchers.Main) {
                        dialog.setMessage("Cap Found")
                        withContext(Dispatchers.IO)
                        {
                            delay(10000)
                            theViewModel.checkIfCapAccepted()
                            delay(5000)
                            withContext(Dispatchers.Main){
                                theViewModel.theCaptainAnswer.observe(viewLifecycleOwner,{
                                    it?.let {
                                        if (it=="yes") {
                                            theViewModel.updateAfterCaptainAcceptance()
                                            val theWaitingIntent= Intent(context, CalculatingWaitingTimeService::class.java).apply {
                                                action=Utility.startWaitingService
                                            }
                                            ContextCompat.startForegroundService(context, theWaitingIntent)
                                            theGeoQuery.removeAllListeners()
                                            dialog.dismiss()
                                        }
                                        else {
                                            theViewModel.updateAfterCaptainRejection()
                                            lifecycleScope.launch(Dispatchers.IO) {
                                                findCaptainsWithRecursion(locationref,theViewModel.geoRefrence!!,requireContext(),dialog)                                        }
                                        }
                                    }
                                })
                            }
                        }
                    }

                    theViewModel. isFound = true
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
                        findCaptainsWithRecursion(locationref,geoLocation,context,dialog)
                    }
                }
                else if (theViewModel.theDistance> theViewModel.theRadiouslLimit)
                {
                    dialog.dismiss()
                    theViewModel.couldntFindCaptainsNearBy()
                    Toast.makeText(context, "we couldn't Find Captains here", Toast.LENGTH_SHORT).show()
                    theGeoQuery.removeAllListeners()
                    theViewModel.resetBeforeSearching()
                    return
                }

            }

            override fun onGeoQueryError(error: DatabaseError?) {
            }
        })


    }






    }

//trash
/*   val theStatus =   fireBaseDataBase.child("CustomersPending").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Cap is Coming").child(theViewModel.theDriver!!)
                   theStatus .addValueEventListener(object : ValueEventListener {
                       override fun onDataChange(snapshot: DataSnapshot) {
                           if (snapshot.exists()) {
                           *//*    var theValue = snapshot.value.toString()
                                        if (theValue == "yes") {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                withContext(Dispatchers.IO){
                                                    fireBaseDataBase.child("ConfirmedForCst").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(theViewModel.theDriver!!).addOnSuccessListener {
                                                        fireBaseDataBase.child("CustomersPending").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Cap is Coming").removeValue()
                                                    }
                                                    fireBaseDataBase.child("Customers").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Status").setValue("Waiting")
                                                    createThePendingRideDetails(theDriver!!,FirebaseAuth.getInstance().currentUser!!.uid).addOnSuccessListener {
                                                        val theWaitingIntent= Intent(context, CalculatingWaitingTimeService::class.java).apply {
                                                            action=Utility.startWaitingService
                                                        }
                                                        ContextCompat.startForegroundService(context, theWaitingIntent)
                                                        theGeoQuery.removeAllListeners()

                                                    }

                                                }


                                            }
                                            theStatus.removeEventListener(this)
                                            dialog.dismiss()*//*
                                        } else
                                        {

                                            CoroutineScope(Dispatchers.IO).launch {
                                                makeTheCapStatusOff(theDriver!!)
                                                fireBaseDataBase.child("Pending For Caps").child(theViewModel.theDriver!!).removeValue()
                                                restBeforeSearching()
                                                findCaptainswithRecursion(locationref, geoLocation, context, dialog)

                                                dialog.dismiss()
                                            }
                                            theStatus.removeEventListener(this)
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }

                            })
*/