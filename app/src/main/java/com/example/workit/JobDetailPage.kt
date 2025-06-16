package com.example.workit

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.text.Html
import android.os.Build

data class SavedJob(
    val jobName: String,
    val companyName: String,
    val companyLogo: Int,
    val category: String,
    val logoUrl: String? = null,
    val jobId: Int? = null,
    val location: String? = null,
    val description: String? = null,
    val requirements: String? = null,
    val url: String? = null
)

class JobDetailPage : AppCompatActivity() {
    private var currentCompanyName: String = ""
    private var isSaved: Boolean = false
    private var isApplied: Boolean = false
    private lateinit var saveButton: ImageButton
    private lateinit var applyButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private var currentJob: SavedJob? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_job_detail_page)

        sharedPreferences = getSharedPreferences("saved_jobs", MODE_PRIVATE)

        // Get data from intent
        val jobName = intent.getStringExtra("job_name") ?: "Job Title"
        val companyName = intent.getStringExtra("company_name") ?: "Company Name"
        val companyLogo = intent.getIntExtra("company_logo", R.drawable.placeholder_company_logo)
        val category = intent.getStringExtra("category") ?: "Category"
        val logoUrl = intent.getStringExtra("logoUrl")
        val jobId = intent.getIntExtra("job_id", -1).takeIf { it != -1 }
        val location = intent.getStringExtra("location")
        val description = intent.getStringExtra("description")
        val url = intent.getStringExtra("url")
        val minSalary = intent.getFloatExtra("min_salary", -1f).takeIf { it != -1f }
        val maxSalary = intent.getFloatExtra("max_salary", -1f).takeIf { it != -1f }
        val currency = intent.getStringExtra("currency")
        val requirements = intent.getStringExtra("requirements")

        currentCompanyName = companyName
        currentJob = SavedJob(
            jobName = jobName,
            companyName = companyName,
            companyLogo = companyLogo,
            category = category,
            logoUrl = logoUrl,
            jobId = jobId,
            location = location,
            description = description,
            requirements = requirements,
            url = url
        )

        setupButtons()
        updateJobData(
            jobName,
            companyName,
            companyLogo,
            category,
            logoUrl,
            location,
            description,
            minSalary,
            maxSalary,
            currency,
            requirements
        )
        setupReviewSection()
        checkIfJobIsSaved()
        checkIfJobIsApplied()
    }

    private fun setupButtons() {
        try {
            val backButton = findViewById<ImageButton>(R.id.btn_back)
            backButton.setOnClickListener {
                finish()
            }

            saveButton = findViewById<ImageButton>(R.id.btn_save)
            saveButton.setOnClickListener {
                toggleSaveJob()
            }

            applyButton = findViewById<Button>(R.id.btn_apply_job)
            applyButton.setOnClickListener {
                applyForJob()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyForJob() {
        currentJob?.let { job ->
            if (isApplied) {
                Toast.makeText(this, "You have already applied for this job!", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            addJobToApplied(job)
            isApplied = true
            updateApplyButtonState()

            val jobName = job.jobName
            Toast.makeText(this, "Applied for $jobName successfully!", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkIfJobIsApplied() {
        currentJob?.let { job ->
            val appliedJobs = getAppliedJobs()
            isApplied = appliedJobs.any {
                it.jobName == job.jobName && it.companyName == job.companyName
            }
            updateApplyButtonState()
        }
    }

    private fun updateApplyButtonState() {
        if (isApplied) {
            applyButton.text = "Applied ✓"
            applyButton.isEnabled = false
            applyButton.alpha = 0.6f
        } else {
            applyButton.text = "Apply Job"
            applyButton.isEnabled = true
            applyButton.alpha = 1.0f
        }
    }

    private fun addJobToApplied(job: SavedJob) {
        val appliedJobs = getAppliedJobs().toMutableList()
        if (!appliedJobs.any { it.jobName == job.jobName && it.companyName == job.companyName }) {
            appliedJobs.add(job)
            saveAppliedJobs(appliedJobs)
        }
    }

    private fun getAppliedJobs(): List<SavedJob> {
        val json = sharedPreferences.getString("applied_jobs_list", "[]")
        val type = object : TypeToken<List<SavedJob>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    private fun saveAppliedJobs(jobs: List<SavedJob>) {
        val json = Gson().toJson(jobs)
        sharedPreferences.edit().putString("applied_jobs_list", json).apply()
    }

    private fun checkIfJobIsSaved() {
        currentJob?.let { job ->
            val savedJobs = getSavedJobs()
            isSaved = savedJobs.any {
                it.jobName == job.jobName && it.companyName == job.companyName
            }
            updateSaveButtonState()
        }
    }

    private fun toggleSaveJob() {
        currentJob?.let { job ->
            if (isSaved) {
                removeJobFromSaved(job)
                isSaved = false
                Toast.makeText(this, "Removed from saved jobs", Toast.LENGTH_SHORT).show()
            } else {
                addJobToSaved(job)
                isSaved = true
                Toast.makeText(this, "Saved to your profile", Toast.LENGTH_SHORT).show()
            }
            updateSaveButtonState()
        }
    }

    private fun updateSaveButtonState() {
        if (isSaved) {
            saveButton.setImageResource(R.drawable.bookmark_fill)
        } else {
            saveButton.setImageResource(R.drawable.bookmark_button)
        }
    }

    private fun addJobToSaved(job: SavedJob) {
        val savedJobs = getSavedJobs().toMutableList()
        savedJobs.add(job)
        saveSavedJobs(savedJobs)
    }

    private fun removeJobFromSaved(job: SavedJob) {
        val savedJobs = getSavedJobs().toMutableList()
        savedJobs.removeAll {
            it.jobName == job.jobName && it.companyName == job.companyName
        }
        saveSavedJobs(savedJobs)
    }

    private fun getSavedJobs(): List<SavedJob> {
        val json = sharedPreferences.getString("saved_jobs_list", "[]")
        val type = object : TypeToken<List<SavedJob>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    private fun saveSavedJobs(jobs: List<SavedJob>) {
        val json = Gson().toJson(jobs)
        sharedPreferences.edit().putString("saved_jobs_list", json).apply()
    }

    private fun setupReviewSection() {
        try {
            val reviewSection = findViewById<LinearLayout>(R.id.ll_review_section)
            reviewSection.setOnClickListener {
                val intent = Intent(this, ReviewPage::class.java)
                intent.putExtra("company_name", currentCompanyName)
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error setting up review section", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cleanHtmlText(htmlText: String?): String {
        if (htmlText.isNullOrEmpty()) return ""

        var cleanText = htmlText

        cleanText = cleanText.replace(Regex("<[^>]*>"), "")

        cleanText = cleanText.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace("&ndash;", "–")
            .replace("&mdash;", "—")

        cleanText = cleanText.replace(Regex("\\s+"), " ")

        cleanText = cleanText.replace("</p><p>", "\n\n")
            .replace("<br/>", "\n")
            .replace("<br>", "\n")
            .replace("<br />", "\n")

        return cleanText.trim()
    }

    private fun updateJobData(
        jobName: String,
        companyName: String,
        companyLogo: Int,
        category: String,
        logoUrl: String? = null,
        location: String? = null,
        description: String? = null,
        minSalary: Float? = null,
        maxSalary: Float? = null,
        currency: String? = null,
        requirements: String? = null
    ) {
        try {
            val ivCompanyLogo = findViewById<ImageView>(R.id.iv_logo)

            if (!logoUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(logoUrl)
                    .placeholder(companyLogo)
                    .error(companyLogo)
                    .into(ivCompanyLogo)
            } else {
                ivCompanyLogo.setImageResource(companyLogo)
            }

            val tvCompanyName = findViewById<TextView>(R.id.tv_company_name)
            tvCompanyName.text = companyName

            val tvJobName = findViewById<TextView>(R.id.tv_job_type)
            tvJobName.text = jobName

            location?.let {
                try {
                    val tvLocation = findViewById<TextView>(R.id.tv_location)
                    tvLocation.text = it
                    tvLocation.visibility = View.VISIBLE
                } catch (e: Exception) {
                }
            }

            description?.let {
                try {
                    val tvDescription = findViewById<TextView>(R.id.tv_description)
                    // Clean HTML tags for better display
                    val cleanDescription = cleanHtmlText(it)
                    tvDescription.text = cleanDescription
                } catch (e: Exception) {
                }
            }

            requirements?.let {
                try {
                    val tvRequirements = findViewById<TextView>(R.id.tv_req)
                    val cleanRequirements = it.replace("<br/>", "\n")
                        .replace("<br>", "\n")
                        .replace("&amp;", "&")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                    tvRequirements.text = cleanRequirements
                } catch (e: Exception) {
                }
            } ?: run {

                try {
                    val tvRequirements = findViewById<TextView>(R.id.tv_req)
                    tvRequirements.text = "No specific requirements listed for this position."
                } catch (e: Exception) {
                }
            }


            if (minSalary != null && maxSalary != null && currency != null) {
                try {
                    val tvSalary = findViewById<TextView>(R.id.tv_salary)
                    tvSalary.text = "${currency} ${minSalary.toInt()} - ${maxSalary.toInt()}"
                    tvSalary.visibility = View.VISIBLE
                } catch (e: Exception) {
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Viewing: $jobName at $companyName", Toast.LENGTH_LONG).show()
        }
    }
}