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
import org.springframework.web.multipart.MultipartFile;

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

    public Application getApplicationById(String id) {
        return applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
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

    /**
     * Candidate applies to a job. The resume is attached here, at the
     * point of application — there is no separate/central resume upload.
     */
    public Application applyToJob(String jobId, String userId,
            MultipartFile resume, String coverLetter) throws Exception {

        if (applicationRepository.existsByUserIdAndJobId(userId, jobId)) {
            throw new RuntimeException("You have already applied for this job");
        }

        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Application application = new Application();
        application.setJobId(jobId);
        application.setUserId(userId);
        String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") +
            " " + (user.getLastName() != null ? user.getLastName() : "")).trim();
        application.setCandidateName(fullName);
        application.setCandidateEmail(user.getEmail());
        application.setCandidatePhone(user.getPhone());
        application.setCoverLetter(coverLetter);

        if (resume != null && !resume.isEmpty()) {
            String base64Data = java.util.Base64.getEncoder().encodeToString(resume.getBytes());
            application.setResumeData(base64Data);
            application.setResumeFileName(resume.getOriginalFilename());
            application.setResumeContentType(resume.getContentType());

            // Keep the candidate's profile in sync with their latest resume
            user.setResumeData(base64Data);
            user.setResumeContentType(resume.getContentType());
            user.setResumeName(resume.getOriginalFilename());
            user.setResumeUploaded(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        int matchScore = recommendationService.calculateMatchScore(user, job);
        application.setMatchScore(matchScore);

        Application saved = applicationRepository.save(application);

        job.setApplicationsCount(job.getApplicationsCount() + 1);
        jobRepository.save(job);

        return saved;
    }

    /**
     * Full applicant list (candidate profile + application data) for a
     * job, used by the recruiter dashboard's "View Applicants" modal.
     */
    public List<CandidateProfile> getJobApplicantsProfiles(String jobId) {
        List<Application> applications =
            applicationRepository.findByJobIdOrderByMatchScoreDesc(jobId);

        List<CandidateProfile> profiles = new ArrayList<>();
        for (Application app : applications) {
            CandidateProfile profile = new CandidateProfile();
            profile.setApplicationId(app.getId());
            profile.setJobId(app.getJobId());
            profile.setUserId(app.getUserId());
            profile.setStatus(app.getStatus());
            profile.setMatchScore(app.getMatchScore());
            profile.setCoverLetter(app.getCoverLetter());
            profile.setRecruiterNotes(app.getRecruiterNotes());
            profile.setAppliedAt(app.getAppliedAt());
            profile.setUpdatedAt(app.getUpdatedAt());
            profile.setPhone(app.getCandidatePhone());

            userRepository.findById(app.getUserId()).ifPresentOrElse(user -> {
                profile.setFirstName(user.getFirstName());
                profile.setLastName(user.getLastName());
                profile.setEmail(user.getEmail());
                profile.setSkills(user.getSkills());
                profile.setExperience(user.getExperience());
                profile.setEducation(user.getEducation());
                profile.setResumeName(user.getResumeName());
                profile.setLinkedinUrl(user.getLinkedinUrl());
            }, () -> {
                String name = app.getCandidateName() != null ? app.getCandidateName() : "";
                String[] parts = name.split(" ", 2);
                profile.setFirstName(parts.length > 0 ? parts[0] : "");
                profile.setLastName(parts.length > 1 ? parts[1] : "");
                profile.setEmail(app.getCandidateEmail());
            });

            profiles.add(profile);
        }
        return profiles;
    }
}
