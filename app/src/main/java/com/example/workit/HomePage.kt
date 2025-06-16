package com.example.workit

import retrofit2.Response
import android.content.Intent
import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workit.databinding.ActivityHomePageBinding
import com.example.workit.model.CompanyItem
import com.example.workit.model.Job
import com.example.workit.model.JobItem
import com.example.workit.model.RemoteJob
import com.example.workit.utils.ApiClient
import com.example.workit.utils.CompanyAdapter
import com.example.workit.utils.JobAdapter
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import java.io.IOException

class HomePage : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding
    private lateinit var jobAdapter: JobAdapter
    private lateinit var companyAdapter: CompanyAdapter
    private lateinit var recyclerView: RecyclerView
    private val selectedCompanyFilters = mutableSetOf<String>()
    private val selectedJobFilters = mutableSetOf<String>()
    private val allJobItems = mutableListOf<JobItem>()
    private val allApiJobs = mutableListOf<Job>()
    private var isUsingApiData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.rvListjob

        val rvJobs = binding.rvListjob
        val rvCompanies = binding.rvListcompany

        rvJobs.layoutManager = LinearLayoutManager(this)
        rvCompanies.layoutManager = LinearLayoutManager(this)

        jobAdapter = JobAdapter()
        companyAdapter = CompanyAdapter(getCompanyList().toMutableList())

        rvJobs.adapter = jobAdapter
        rvCompanies.adapter = companyAdapter


        val navView: BottomNavigationView = binding.navView

        navView.selectedItemId = R.id.navigation_home

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    true
                }

                R.id.navigation_saved -> {
                    val intent = Intent(this, SavedPage::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.navigation_discover -> {
                    val intent = Intent(this, CommunityPage::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfilePage::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        val tabs: TabLayout = binding.tabs

//        val btnPartTime = binding.jobFilter.findViewById<Button>(R.id.btn_part_time)
//        val btnIntern = binding.jobFilter.findViewById<Button>(R.id.btn_intern)
//        val btnFulltime = binding.jobFilter.findViewById<Button>(R.id.btn_fulltime)

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        rvJobs.visibility = View.VISIBLE
                        rvCompanies.visibility = View.GONE
                        binding.search.queryHint = "Search jobs..."
                        binding.jobFilter.visibility = View.VISIBLE
                        binding.companyFilter.visibility = View.GONE
                    }

                    1 -> {
                        rvJobs.visibility = View.GONE
                        rvCompanies.visibility = View.VISIBLE
                        binding.search.queryHint = "Search companies..."
                        binding.jobFilter.visibility = View.GONE
                        binding.companyFilter.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        tabs.getTabAt(0)?.select()
        rvJobs.visibility = View.VISIBLE
        rvCompanies.visibility = View.GONE
        binding.search.queryHint = "Search jobs..."
        binding.jobFilter.visibility = View.VISIBLE
        binding.companyFilter.visibility = View.GONE

        loadLocalJobs()

        fetchRemoteOkJobs()
        setupJobFilters()
        setupCompanyFilters()
        setupSearch()
    }

    private fun loadLocalJobs() {
        val localJobs = getJobList()
        allJobItems.clear()
        allJobItems.addAll(localJobs)
        jobAdapter.updateLocalData(localJobs)
        isUsingApiData = false
    }

    private fun setupJobFilters() {
        val btnPartTime = binding.jobFilter.findViewById<Button>(R.id.btn_part_time)
        val btnIntern = binding.jobFilter.findViewById<Button>(R.id.btn_intern)
        val btnFulltime = binding.jobFilter.findViewById<Button>(R.id.btn_fulltime)

        btnPartTime.setOnClickListener {
            toggleJobFilter(btnPartTime, "Part-time")
            applyJobFilters()
        }

        btnIntern.setOnClickListener {
            toggleJobFilter(btnIntern, "Intern")
            applyJobFilters()
        }

        btnFulltime.setOnClickListener {
            toggleJobFilter(btnFulltime, "Fulltime")
            applyJobFilters()
        }
    }

    private fun toggleJobFilter(button: Button, filterValue: String) {
        button.isSelected = !button.isSelected

        if (button.isSelected) {
            selectedJobFilters.add(filterValue)
            button.text = "${button.text.toString().replace(" ✕", "")} ✕"
            button.setTextColor(resources.getColor(android.R.color.white, theme))
        } else {
            selectedJobFilters.remove(filterValue)
            button.text = button.text.toString().replace(" ✕", "")
            button.setTextColor(resources.getColor(R.color.darkgray2, theme))
        }
    }

    private fun applyJobFilters() {
        if (isUsingApiData) {
            if (selectedJobFilters.isEmpty()) {
                jobAdapter.updateFromApi(allApiJobs)
            } else {
                val filteredJobs = allApiJobs.filter { job ->
                    val category = determineJobCategory(job)
                    selectedJobFilters.contains(category)
                }
                jobAdapter.updateFromApi(filteredJobs)
            }
        } else {
            if (selectedJobFilters.isEmpty()) {
                jobAdapter.updateLocalData(allJobItems)
            } else {
                val filteredJobs = allJobItems.filter { job ->
                    selectedJobFilters.contains(job.category)
                }
                jobAdapter.updateLocalData(filteredJobs)
            }
        }
    }

    private fun determineJobCategory(job: Job): String {
        return when {
            job.jobTitle.contains("intern", ignoreCase = true) -> "Intern"
            job.jobTitle.contains("part", ignoreCase = true) -> "Part-time"
            else -> "Fulltime"
        }
    }

    private fun setupCompanyFilters() {
        val btnEcomm = binding.companyFilter.findViewById<Button>(R.id.btn_ecomm)
        val btnTech = binding.companyFilter.findViewById<Button>(R.id.btn_tech)
        val btnFintech = binding.companyFilter.findViewById<Button>(R.id.btn_fintech)

        btnEcomm.setOnClickListener {
            toggleFilter(btnEcomm, "E-Commerce")
            applyCompanyFilters()
        }

        btnTech.setOnClickListener {
            toggleFilter(btnTech, "Technology")
            applyCompanyFilters()
        }

        btnFintech.setOnClickListener {
            toggleFilter(btnFintech, "Financial")
            applyCompanyFilters()
        }
    }

    private fun toggleFilter(button: Button, filterValue: String) {
        button.isSelected = !button.isSelected

        if (button.isSelected) {
            selectedCompanyFilters.add(filterValue)
            button.text = "${button.text.toString().replace(" ✕", "")} ✕"
            button.setTextColor(resources.getColor(android.R.color.white, theme))
        } else {
            selectedCompanyFilters.remove(filterValue)
            button.text = button.text.toString().replace(" ✕", "")
            button.setTextColor(resources.getColor(R.color.darkgray2, theme))
        }
    }

    private fun applyCompanyFilters() {
        if (selectedCompanyFilters.isEmpty()) {
            companyAdapter.updateData(getCompanyList().toMutableList())
        } else {
            val filteredCompanies = getCompanyList().filter { company ->
                selectedCompanyFilters.any { filter ->
                    company.industry.contains(filter, ignoreCase = true)
                }
            }
            companyAdapter.updateData(filteredCompanies.toMutableList())
        }
    }

    private fun setupSearch() {
        binding.search.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterBySearchQuery(newText)
                return true
            }
        })
    }

    private fun filterBySearchQuery(query: String?) {
        if (query.isNullOrEmpty()) {
            if (binding.tabs.selectedTabPosition == 0) {
                applyJobFilters()
            } else {
                applyCompanyFilters()
            }
        } else {
            if (binding.tabs.selectedTabPosition == 0) {
                if (isUsingApiData) {
                    // Filter API data
                    val filteredBySearch = allApiJobs.filter { job ->
                        job.jobTitle.contains(query, ignoreCase = true) ||
                                job.employerName.contains(query, ignoreCase = true) ||
                                job.locationName.contains(query, ignoreCase = true)
                    }
                    jobAdapter.updateFromApi(filteredBySearch)
                } else {
                    // Filter local data
                    val filteredBySearch = allJobItems.filter { job ->
                        job.job_name.contains(query, ignoreCase = true) ||
                                job.company_name.contains(query, ignoreCase = true) ||
                                job.category.contains(query, ignoreCase = true)
                    }
                    jobAdapter.updateLocalData(filteredBySearch)
                }
            } else {
                val filteredBySearch = getCompanyList().filter { company ->
                    company.company_name.contains(query, ignoreCase = true) ||
                            company.industry.contains(query, ignoreCase = true)
                }

                val filteredFinal = if (selectedCompanyFilters.isEmpty()) {
                    filteredBySearch
                } else {
                    filteredBySearch.filter { company ->
                        selectedCompanyFilters.any { filter ->
                            company.industry.contains(filter, ignoreCase = true)
                        }
                    }
                }

                companyAdapter.updateData(filteredFinal.toMutableList())
            }
        }
    }

    private fun getJobList(): List<JobItem> {
        return listOf(
            JobItem("UI/UX Designer", "BCA", R.drawable.bca_logo, "Intern"),
            JobItem("Software Engineer", "Shopee", R.drawable.shopee_logo, "Intern"),
            JobItem("Bank Manager", "OCBC Bank", R.drawable.ocbc_logo, "Fulltime"),
            JobItem("Barista", "Starbucks", R.drawable.starbucks_logo, "Part-time")
        )
    }

    private fun getCompanyList(): List<CompanyItem> {
        return listOf(
            CompanyItem("BCA", "Financial Technology", R.drawable.bca_logo),
            CompanyItem("Shopee", "E-commerce", R.drawable.shopee_logo),
            CompanyItem("OCBC Bank", "Financial Technology", R.drawable.ocbc_logo),
            CompanyItem("Starbucks", "Food & Beverage", R.drawable.starbucks_logo)
        )
    }

    private fun fetchRemoteOkJobs() {
        ApiClient.remoteOkService.getJobs()
            .enqueue(object : Callback<List<RemoteJob>> {
                override fun onResponse(
                    call: Call<List<RemoteJob>>,
                    response: Response<List<RemoteJob>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val remoteJobs = response.body()!!.drop(1)

                        Log.d("RemoteOK", "Jobs received: ${remoteJobs.size}")
                        if (remoteJobs.isNotEmpty()) {
                            Log.d(
                                "RemoteOK",
                                "First job: ${remoteJobs[0].position} at ${remoteJobs[0].company}"
                            )
                        }

                        val apiJobs = remoteJobs.map { remoteJob ->
                            Job(
                                jobId = remoteJob.id,
                                employerName = remoteJob.company ?: "Unknown Company",
                                jobTitle = remoteJob.position ?: "Unknown Position",
                                locationName = "Remote", // RemoteOK jobs are remote
                                minimumSalary = null,
                                maximumSalary = null,
                                currency = "USD",
                                expirationDate = "",
                                description = remoteJob.description ?: "",
                                requirements = "",
                                url = "",
                                logoUrl = remoteJob.logo?.takeIf {
                                    it.startsWith("http") && (it.endsWith(".png") || it.endsWith(".jpg") || it.endsWith(
                                        ".svg"
                                    ))
                                }
                            )
                        }

                        allApiJobs.clear()
                        allApiJobs.addAll(apiJobs)

                        isUsingApiData = true
                        applyJobFilters()

                        Toast.makeText(
                            this@HomePage,
                            "Loaded ${apiJobs.size} remote jobs!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e("RemoteOK", "API error: ${response.code()}")
                        Toast.makeText(
                            this@HomePage,
                            "Failed to load remote jobs, showing local jobs",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<RemoteJob>>, t: Throwable) {
                    Log.e("RemoteOK", "API failure: ${t.message}")
                    Toast.makeText(
                        this@HomePage,
                        "Failed to load remote jobs: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

}