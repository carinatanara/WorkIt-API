package com.example.workit.utils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    val remoteOkService: RemoteOkService by lazy {
        Retrofit.Builder()
            .baseUrl("https://remoteok.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RemoteOkService::class.java)
    }
}
