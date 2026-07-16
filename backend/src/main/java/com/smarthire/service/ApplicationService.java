package com.smarthire.service;

import com.smarthire.dto.BulkStatusRequest;
import com.smarthire.dto.CandidateProfile;
import com.smarthire.model.Application;
import com.smarthire.model.Job;
import com.smarthire.model.User;
import com.smarthire.repository.ApplicationRepository;
import com.smarthire.repository.JobRepository;
import com.smarthire.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecommendationService recommendationService;

    public Application submitApplication(Application application) {
        if (applicationRepository.existsByUserIdAndJobId(
                application.getUserId(), application.getJobId())) {
            throw new RuntimeException("You have already applied for this job");
        }
        int matchScore = recommendationService.calculateMatchScore(application);
        application.setMatchScore(matchScore);

        Job job = jobRepository.findById(application.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setApplicationsCount(job.getApplicationsCount() + 1);
        jobRepository.save(job);

        return applicationRepository.save(application);
    }

    public List<Application> getUserApplications(String userId) {
        return applicationRepository.findByUserId(userId);
    }

    public List<Application> getApplicationsByStatus(String status) {
        return applicationRepository.findByStatus(status);
    }

    public List<Application> getJobApplicationsSorted(String jobId, String sortBy) {
        List<Application> applications = applicationRepository.findByJobId(jobId);

        if ("matchScore".equals(sortBy)) {
            applications.sort(Comparator
                .comparingInt((Application a) ->
                    a.getMatchScore() != null ? a.getMatchScore() : 0)
                .reversed());
        } else if ("date".equals(sortBy)) {
            applications.sort(Comparator
                .comparing(Application::getAppliedAt).reversed());
        }
        return applications;
    }

    public List<Application> getJobApplications(String jobId) {
        return applicationRepository.findByJobIdOrderByMatchScoreDesc(jobId);
    }

    public List<Application> getJobApplicationsByStatus(
            String jobId, String status) {
        return applicationRepository.findByJobIdAndStatus(jobId, status);
    }

    public Application updateApplicationStatus(String id, String status) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }

    public List<Application> bulkUpdateStatus(BulkStatusRequest request) {
        List<Application> updated = new ArrayList<>();
        for (String appId : request.getApplicationIds()) {
            applicationRepository.findById(appId).ifPresent(app -> {
                app.setStatus(request.getStatus());
                app.setUpdatedAt(LocalDateTime.now());
                updated.add(applicationRepository.save(app));
            });
        }
        return updated;
    }

    public CandidateProfile getCandidateFullProfile(String applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        User user = userRepository.findById(application.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        CandidateProfile profile = new CandidateProfile();
        profile.setUserId(user.getId());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setEmail(user.getEmail());
        profile.setPhone(user.getPhone());
        profile.setSkills(user.getSkills());
        profile.setExperience(user.getExperience());
        profile.setEducation(user.getEducation());
        profile.setResumeUrl(user.getResumeUrl());
        profile.setResumeName(user.getResumeName());
        profile.setLinkedinUrl(user.getLinkedinUrl());
        profile.setApplicationId(application.getId());
        profile.setJobId(application.getJobId());
        profile.setStatus(application.getStatus());
        profile.setMatchScore(application.getMatchScore());
        profile.setCoverLetter(application.getCoverLetter());
        profile.setRecruiterNotes(application.getRecruiterNotes());
        profile.setAppliedAt(application.getAppliedAt());
        profile.setUpdatedAt(application.getUpdatedAt());
        return profile;
    }

    public Application updateRecruiterNotes(String applicationId, String notes) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        application.setRecruiterNotes(notes);
        application.setUpdatedAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }

    public List<Application> getAllApplicationsForRecruiter(String recruiterId) {
        List<Job> recruiterJobs = jobRepository.findByPostedBy(recruiterId);
        if (recruiterJobs.isEmpty()) return new ArrayList<>();

        List<String> jobIds = new ArrayList<>();
        for (Job job : recruiterJobs) jobIds.add(job.getId());
        return applicationRepository.findByJobIdIn(jobIds);
    }
}
