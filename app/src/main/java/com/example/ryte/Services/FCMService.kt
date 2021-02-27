package com.example.ryte.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.Ui.MainActivity
import com.example.ryte.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random


class FCMService: FirebaseMessagingService() {
    private  val myChannelId = "my_channelIdForRyte"

    var player: MediaPlayer?= null

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        if (FirebaseAuth.getInstance().currentUser != null) {
            token = p0
            updateTheCurrentUsertoken()
        }

    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        player=  MediaPlayer.create(this, R.raw.notificationtone)
        player?.let { it ->
            it.start()
            it.setOnCompletionListener {it1->
                it1.release()
                player= null
            }

        }
        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)

        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, myChannelId)
            .setContentTitle(p0.data["title"])
            .setContentText(p0.data["message"])
            .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(myChannelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.GREEN

        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private var token: String? = null
        //  fun getTheToken(): String = token!!
        fun updateTheCurrentUsertoken() {

            if (FirebaseAuth.getInstance().currentUser != null) FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {

                FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val theType: String = snapshot.value.toString()
                                if (theType == "Cst") {
                                    FirebaseDatabase.getInstance().reference.child("Users Tokens").child("Customers Tokens")
                                        .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(it.token)
                                    val TOPIC = "/topics/${it.token}"
                                    FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
                                } else {
                                    FirebaseDatabase.getInstance().reference.child("Users Tokens").child("Captains Tokens")
                                        .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(it.token)
                                    val TOPIC = "/topics/${it.token}"
                                    FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    }
                )

            }

        }
    }
}