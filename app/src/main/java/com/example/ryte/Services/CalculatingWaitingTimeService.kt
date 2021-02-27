package com.example.ryte.Services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.ryte.Others.Utility
import com.example.ryte.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalculatingWaitingTimeService: Service() {
    val waitingForCst by lazy { 600000L }
    val waitingForCap by lazy { 300000L }
    private var theCounter: CountDownTimer? = null

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {

                Utility.startWaitingService -> {
                    startItAsForGround()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        theCounter?.cancel()
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun startItAsForGround() {
        FirebaseDatabase.getInstance().reference.child("Users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val theType = snapshot.value.toString()
                            FirebaseDatabase.getInstance().reference.child("Users")
                                    .child(FirebaseAuth.getInstance().currentUser!!.uid).removeEventListener(this)
                            if (theType == "Cst") {

                                theCounter = object : CountDownTimer(waitingForCst, 1000) {
                                    override fun onTick(p0: Long) {
                                    }

                                    override fun onFinish() {
                                        FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(Utility.thePendingRideId!!)
                                                .child("CaptainArrived").addValueEventListener(object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        if (snapshot.exists()) {
                                                            if (snapshot.value.toString() == "Nope") {
                                                                FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(Utility.thePendingRideId!!).child("CancelableWithFees").setValue("false")
                                                            } else {

                                                                FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(Utility.thePendingRideId!!).child("CancelableWithFees").setValue("true")

                                                            }

                                                        }

                                                        FirebaseDatabase.getInstance().reference.child("Rides").child("Pending").child(Utility.thePendingRideId!!)
                                                                .child("CaptainArrived").removeEventListener(this)

                                                        stopSelf()

                                                    }

                                                    override fun onCancelled(error: DatabaseError) {
                                                    }

                                                })
                                    }

                                }.start()

                            } else {
                                theCounter = object : CountDownTimer(waitingForCap, 1000) {
                                    override fun onTick(p0: Long) {
                                    }

                                    override fun onFinish() {

                                        FirebaseDatabase.getInstance().reference.child("CancelableRides").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue("yes").addOnSuccessListener {
                                            stopSelf()
                                        }

                                    }

                                }.start()
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createTheNotificationChannel(notificationManager)
        }
        val theNotification = NotificationCompat.Builder(this, Utility.notificationChannelIDForWaitingService).apply {
            setContentTitle("Waiting..")
            setAutoCancel(false)
            setOngoing(true)
            setContentText("Waiting Time Started")
            setSmallIcon(R.drawable.ic_baseline_timer_24)
        }.build()
        startForeground(Utility.notificationIdForWaitingService, theNotification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createTheNotificationChannel(notificationManager: NotificationManager) {

        val serviceChannel = NotificationChannel(
                Utility.notificationChannelIDForWaitingService, Utility.notificationNameForWaitingService, NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel);
        notificationManager.createNotificationChannel(serviceChannel)
    }
}