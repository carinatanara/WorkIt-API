package com.example.workit.model

data class Job (
    val jobId: Int,
    val employerName: String,
    val jobTitle: String,
    val locationName: String,
    val minimumSalary: Float?,
    val maximumSalary: Float?,
    val currency: String,
    val expirationDate: String,
    val description: String,
    val requirements : String,
    val url: String,
    val logoUrl: String?
)