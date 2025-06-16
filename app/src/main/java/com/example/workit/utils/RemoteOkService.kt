package com.example.workit.utils

import com.example.workit.model.RemoteJob
import retrofit2.Call
import retrofit2.http.GET

interface RemoteOkService {
    @GET("api")
    fun getJobs(): Call<List<RemoteJob>>
}
