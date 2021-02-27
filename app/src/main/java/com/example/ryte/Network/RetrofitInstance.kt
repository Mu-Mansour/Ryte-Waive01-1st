package com.example.ryte.Network

import com.example.ryte.Others.Utility.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {

    companion object {
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api: RetrofitInterFace by lazy {
            retrofit.create(RetrofitInterFace::class.java)
        }
    }
}