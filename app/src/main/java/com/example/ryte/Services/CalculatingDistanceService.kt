package com.example.ryte.Services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.ryte.Others.Utility
import com.example.ryte.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CalculatingDistanceService: Service(){
    var thetimer=0
    var firstTime = true
    var theStartingLocation: Location?= null
    var theUserType:String?=null

    lateinit var locationRequest: LocationRequest
    lateinit var locationUpdated: FusedLocationProviderClient
    private val theLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            result?.lastLocation?.let {
                theUserType?.let { _ ->
                    if (firstTime)
                    {
                        theStartingLocation= it
                        updateToSecureMyLastLocation(it)
                        firstTime = false
                    }
                    else {

                        if (it.distanceTo(theStartingLocation!!) >= 16.6) {

                            if (thetimer == 60) {
                                updateToSecureMyLastLocation(it)
                            }
                            if (theUserType=="Cap")
                            {
                                Utility.distance += (it.distanceTo(theStartingLocation).toDouble())
                                theStartingLocation= it
                            }
                            thetimer += 3

                        }
                        else
                        {
                            if (theUserType=="Cap")
                            {
                                Utility.waitingTime += 3.0
                            }
                            if (thetimer == 60) {
                                updateToSecureMyLastLocation(it)
                            }
                            thetimer += 3


                        }


                    }
                }


            }
        }


    }

    @SuppressLint("VisibleForTests", "MissingPermission")
    override fun onCreate() {
        super.onCreate()
        locationUpdated= FusedLocationProviderClient(this)
        locationRequest= LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.fastestInterval= 3000
        locationRequest.interval= 3000

    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action)
            {
                Utility.startRide -> {

                    startItAsForGround()

                }

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        locationUpdated.removeLocationUpdates(theLocationCallBack)
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null    }

    @SuppressLint("MissingPermission")
    private fun startItAsForGround()
    {
        FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    theUserType=snapshot.value.toString()
                    FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).removeEventListener(this)
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        locationUpdated.requestLocationUpdates(locationRequest,theLocationCallBack, Looper.myLooper())

        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            createTheNotificationChannel(notificationManager)
        }
        val theNotification = NotificationCompat.Builder(this,Utility.notificationChannelIDForService).apply {
            setContentTitle("Ryte")
            setAutoCancel(false)
            setOngoing(true)
            setContentText("Ryte is Monitoring Your Ride")
            setSmallIcon(R.drawable.ic_baseline_directions_car_24)
        }.build()
        startForeground(Utility.notificationIdForDistanceService,theNotification)
    }
    fun updateToSecureMyLastLocation(location: Location)
    {
        val theLocationToBeUploaded = LatLng(location.latitude, location.longitude)
        FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists())
                        {
                            FirebaseDatabase.getInstance().reference.child("Last known location").child(snapshot.value!!.toString()).child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(theLocationToBeUploaded).addOnSuccessListener {
                                thetimer = 0
                                FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).removeEventListener(this) }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}

                }
        )

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTheNotificationChannel(notificationManager: NotificationManager)
    {

        val serviceChannel =  NotificationChannel(
                Utility.notificationChannelIDForService,Utility.notificationNameForDistanceService, NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel);
        notificationManager.createNotificationChannel(serviceChannel)
    }


}