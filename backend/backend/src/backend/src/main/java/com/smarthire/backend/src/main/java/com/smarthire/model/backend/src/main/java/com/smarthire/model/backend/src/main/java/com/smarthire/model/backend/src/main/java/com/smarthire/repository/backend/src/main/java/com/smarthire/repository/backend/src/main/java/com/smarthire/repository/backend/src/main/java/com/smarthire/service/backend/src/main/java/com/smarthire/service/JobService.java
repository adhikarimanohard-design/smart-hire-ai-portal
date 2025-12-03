package com.smarthire.service;

import com.smarthire.model.Job;
import com.smarthire.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {
    
    @Autowired
    private JobRepository jobRepository;
    
    public List<Job> getAllActiveJobs() {
        return jobRepository.findByActiveTrue();
    }
    
    public Job getJobById(String id) {
        return jobRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
    }
    
    public List<Job> searchJobs(String query) {
        return jobRepository.findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(query, query);
    }
    
    public Job createJob(Job job) {
        return jobRepository.save(job);
    }
    
    public Job updateJob(String id, Job jobDetails) {
        Job job = getJobById(id);
        
        job.setTitle(jobDetails.getTitle());
        job.setCompany(jobDetails.getCompany());
        job.setLocation(jobDetails.getLocation());
        job.setType(jobDetails.getType());
        job.setSalaryRange(jobDetails.getSalaryRange());
        job.setDescription(jobDetails.getDescription());
        job.setRequirements(jobDetails.getRequirements());
        job.setSkills(jobDetails.getSkills());
        
        return jobRepository.save(job);
    }
    
    public void deleteJob(String id) {
        Job job = getJobById(id);
        job.setActive(false);
        jobRepository.save(job);
    }
    
    public void incrementViewCount(String id) {
        Job job = getJobById(id);
        job.setViewsCount(job.getViewsCount() + 1);
        jobRepository.save(job);
    }
}
