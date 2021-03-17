package com.example.ryte.Others


object Utility {
    const val BASE_URL = "https://fcm.googleapis.com"
    const val FCM_SERVER_KEY = "Your Key"
    const val CONTENT_TYPE = "application/json"
    const val startWaitingService = "startWaitingCst"
    const val startRide = "startRide"
    var theUserType :String?= null
    var distance:Double = 0.0
    var waitingTime:Double = 0.0
    var thePendingRideId:String?=null
    const val notificationChannelIDForService = "notificationIDForDistanceService"
    const val notificationChannelIDForWaitingService = "notificationIDForWaitingService"
    const val notificationNameForDistanceService = "CalculatingDistance"
    const val notificationNameForWaitingService = "CalculatingWaiting"
    const val notificationIdForDistanceService = 91
    const val notificationIdForWaitingService = 919
}