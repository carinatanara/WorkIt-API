package com.example.workit.utils

import android.util.Log
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.workit.JobDetailPage
import com.bumptech.glide.Glide
import com.example.workit.R
import com.example.workit.model.Job
import com.example.workit.model.JobItem

class JobAdapter(private val jobList: MutableList<JobItem> = mutableListOf()) : RecyclerView.Adapter<JobAdapter.ViewHolder>() {

    private var jobItemList: MutableList<JobItem> = mutableListOf()
    private var jobApiList: MutableList<Job> = mutableListOf()
    private var isApiMode = false

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.company_logo)
        val jobTitleView: TextView = itemView.findViewById(R.id.job_title)
        val companyNameView: TextView = itemView.findViewById(R.id.company_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_job, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (isApiMode) {
            bindApiData(holder, position)
        } else {
            bindLocalData(holder, position)
        }
    }

    private fun bindLocalData(holder: ViewHolder, position: Int) {
        val item = jobItemList[position]
        holder.jobTitleView.text = item.job_name
        holder.companyNameView.text = item.company_name

        if (!item.logoUrl.isNullOrEmpty()) {
            Log.d("GlideCheck", "Loading local logo URL: ${item.logoUrl}")
            Glide.with(holder.itemView.context)
                .load(item.logoUrl)
                .placeholder(item.logo)
                .error(item.logo)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(item.logo)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, JobDetailPage::class.java).apply {
                putExtra("job_name", item.job_name)
                putExtra("company_name", item.company_name)
                putExtra("company_logo", item.logo)
                putExtra("category", item.category)
                putExtra("logoUrl", item.logoUrl)
                putExtra("requirements", null as String?) // ADD THIS LINE - local jobs don't have requirements
            }
            context.startActivity(intent)
        }
    }

    private fun bindApiData(holder: ViewHolder, position: Int) {
        val job = jobApiList[position]
        holder.jobTitleView.text = job.jobTitle
        holder.companyNameView.text = job.employerName

        // Load image from API data
        if (!job.logoUrl.isNullOrEmpty()) {
            Log.d("GlideCheck", "Loading API logo: ${job.logoUrl}")
            Glide.with(holder.itemView.context)
                .load(job.logoUrl)
                .placeholder(R.drawable.placeholder_company_logo)
                .error(R.drawable.placeholder_company_logo)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_company_logo)
        }

        // Set click listener for API data
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, JobDetailPage::class.java).apply {
                putExtra("job_name", job.jobTitle)
                putExtra("company_name", job.employerName)
                putExtra("logoUrl", job.logoUrl)
                putExtra("job_id", job.jobId)
                putExtra("location", job.locationName)
                putExtra("min_salary", job.minimumSalary)
                putExtra("max_salary", job.maximumSalary)
                putExtra("currency", job.currency)
                putExtra("description", job.description)
                putExtra("url", job.url)
                putExtra("expiration_date", job.expirationDate)
                putExtra("requirements", null as String?) // ADD THIS LINE - API jobs don't seem to have requirements field
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return if (isApiMode) jobApiList.size else jobItemList.size
    }

    fun updateFromApi(data: List<Job>) {
        isApiMode = true
        jobApiList.clear()
        jobApiList.addAll(data)
        notifyDataSetChanged()
    }

    fun updateLocalData(data: List<JobItem>) {
        isApiMode = false
        jobItemList.clear()
        jobItemList.addAll(data)
        notifyDataSetChanged()
    }

    fun isUsingApiData(): Boolean = isApiMode

    fun clearData() {
        jobItemList.clear()
        jobApiList.clear()
        notifyDataSetChanged()
    }
}