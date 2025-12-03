package com.smarthire.service;

import com.smarthire.model.Application;
import com.smarthire.model.Job;
import com.smarthire.repository.ApplicationRepository;
import com.smarthire.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private RecommendationService recommendationService;
    
    public Application submitApplication(Application application) {
        // Check if user already applied
        if (applicationRepository.existsByUserIdAndJobId(
                application.getUserId(), 
                application.getJobId())) {
            throw new RuntimeException("You have already applied for this job");
        }
        
        // Calculate match score
        int matchScore = recommendationService.calculateMatchScore(application);
        application.setMatchScore(matchScore);
        
        // Update job applications count
        Job job = jobRepository.findById(application.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setApplicationsCount(job.getApplicationsCount() + 1);
        jobRepository.save(job);
        
        return applicationRepository.save(application);
    }
    
    public List<Application> getUserApplications(String userId) {
        return applicationRepository.findByUserId(userId);
    }
    
    public List<Application> getJobApplications(String jobId) {
        return applicationRepository.findByJobId(jobId);
    }
    
    public Application updateApplicationStatus(String id, String status) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        
        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());
        
        return applicationRepository.save(application);
    }
    
    public List<Application> getApplicationsByStatus(String status) {
        return applicationRepository.findByStatus(status);
    }
}
