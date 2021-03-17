package com.example.ryte.Network

import com.example.ryte.Others.Utility.CONTENT_TYPE
import com.example.ryte.Others.Utility.FCM_SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetrofitInterFace {

    @Headers("Authorization: key=$FCM_SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}