package com.example.workit.model

import com.example.workit.R

data class JobItem(
    var job_name: String,
    var company_name: String,
    val logo: Int = R.drawable.placeholder_company_logo,
    val category: String,
    val logoUrl: String? = null

)
