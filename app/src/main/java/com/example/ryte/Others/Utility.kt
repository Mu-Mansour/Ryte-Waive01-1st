package com.example.ryte.Others


object Utility {
    const val BASE_URL = "https://fcm.googleapis.com"
    const val SERVER_KEY = "AAAAIoBj_UE:APA91bF3B08NGVe61jwISMMPMdmpscVulmQWEwBR8eC-HFFGXO04dTBvZqs4SmmusOK-KYWT7A4NQsUc1Cp3YBlNprqL0B3a7hwUFBeCgHyY_VoLIxkdvLDEsPRpTEyc2ZBRDY4M2Zfy"
    const val CONTENT_TYPE = "application/json"
    const val startWaitingService = "startWaitingCst"
    const val startRide = "startRide"
    var theUserType :String?= null
    //for calc distance  service
    var distance:Double = 0.0
    var waitingTime:Double = 0.0
    //for calc Waiting  service
    var thePendingRideId:String?=null
    const val notificationChannelIDForService = "notificationIDForDistanceService"
    const val notificationChannelIDForWaitingService = "notificationIDForWaitingService"
    const val notificationNameForDistanceService = "CalculatingDistance"
    const val notificationNameForWaitingService = "CalculatingWaiting"
    const val notificationIdForDistanceService = 91
    const val notificationIdForWaitingService = 919
}