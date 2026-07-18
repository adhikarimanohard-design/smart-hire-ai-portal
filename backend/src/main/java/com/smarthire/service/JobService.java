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

    @Autowired
    private com.smarthire.repository.ApplicationRepository applicationRepository;

    public List<Job> getAllActiveJobs() {
        return jobRepository.findByActiveTrue();
    }

    public List<Job> searchJobs(String query) {
        return jobRepository
            .findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(
                query, query);
    }

    public Job getJobById(String id) {
        return jobRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "Job not found with id: " + id));
    }

    public void incrementViewCount(String id) {
        Job job = getJobById(id);
        job.setViewsCount(job.getViewsCount() + 1);
        jobRepository.save(job);
    }

    public Job createJob(Job job) {
        return jobRepository.save(job);
    }

    public List<Job> getJobsByRecruiter(String recruiterId) {
        return jobRepository.findByPostedByOrderByPostedDateDesc(recruiterId);
    }

    public List<Job> getActiveJobsByRecruiter(String recruiterId) {
        return jobRepository.findByPostedByAndActiveTrue(recruiterId);
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
        job.setExpiryDate(jobDetails.getExpiryDate());
        return jobRepository.save(job);
    }

    public void deleteJob(String id) {
        Job job = getJobById(id);
        job.setActive(false);
        jobRepository.save(job);
    }

    /**
     * Permanently removes a job posting and all applications submitted
     * against it. Unlike deleteJob() (which just closes the listing so
     * it can be reopened), this is irreversible — used by the "Delete
     * Job" action on the recruiter dashboard.
     */
    public void permanentlyDeleteJob(String id) {
        Job job = getJobById(id); // throws if not found
        applicationRepository.deleteByJobId(id);
        jobRepository.delete(job);
    }

    public Job reopenJob(String id) {
        Job job = getJobById(id);
        job.setActive(true);
        return jobRepository.save(job);
    }

    public long countActiveJobsByRecruiter(String recruiterId) {
        return jobRepository.countByPostedByAndActiveTrue(recruiterId);
    }

    public long countTotalJobsByRecruiter(String recruiterId) {
        return jobRepository.countByPostedBy(recruiterId);
    }
}
