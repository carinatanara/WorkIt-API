package com.example.workit.model

import com.google.gson.annotations.SerializedName

data class RemoteJob(
    val id: Int,
    val company: String?,
    val position: String?,
    val description: String?,
    val tags: List<String>?,
    val logo: String?,

    @SerializedName("job_type")
    val jobType: String?
)
