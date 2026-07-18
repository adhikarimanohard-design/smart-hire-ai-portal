
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ApplicationService {

    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private JobRepository         jobRepository;
    @Autowired private UserRepository        userRepository;
    @Autowired private RecommendationService recommendationService;

    // ── Primary apply: resume saved to DISK, path stored in MongoDB ───────────
    /**
     * FIX FOR "Failed to fetch":
     *
     * Old code: resume → Base64 string (~320 KB) → stored IN MongoDB doc
     *           → huge document → Render free tier timeout → browser error
     *
     * New code: resume → written to disk as a file
     *           → only short path string stored in MongoDB → tiny doc → instant
     *
     * Recruiter downloads resume via GET /api/applications/{id}/resume
     */
    public Application applyToJob(String jobId, String userId,
            MultipartFile resume, String coverLetter) throws Exception {

        // 1. Duplicate guard
        if (applicationRepository.existsByUserIdAndJobId(userId, jobId))
            throw new RuntimeException("You have already applied for this job");

        Job  job  = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Build application
        Application application = new Application();
        application.setJobId(jobId);
        application.setUserId(userId);
        String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") +
            " " + (user.getLastName() != null ? user.getLastName() : "")).trim();
        application.setCandidateName(fullName);
        application.setCandidateEmail(user.getEmail());
        application.setCandidatePhone(user.getPhone());
        application.setCoverLetter(coverLetter != null ? coverLetter : "");

        // 3. Write resume to disk — NOT as Base64 in MongoDB
        if (resume != null && !resume.isEmpty()) {
            String uploadDir = "uploads/resumes/";
            Files.createDirectories(Paths.get(uploadDir));

            String originalName = resume.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                originalName = "resume_" + System.currentTimeMillis();
            }
            String safeOriginal = originalName
                .replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = userId + "_" + jobId + "_" + safeOriginal;
            Path   filePath = Paths.get(uploadDir + fileName);
            Files.write(filePath, resume.getBytes());  // fast local I/O

            // Only the short path goes into MongoDB
            application.setResumeUrl(filePath.toString());
            application.setResumeName(resume.getOriginalFilename());

            // Keep user profile in sync
            user.setResumeName(resume.getOriginalFilename());
            user.setResumeUploaded(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        // 4. AI match score
        int matchScore = recommendationService.calculateMatchScore(user, job);
        application.setMatchScore(matchScore);

        // 5. Save tiny document — instant, no timeout
        Application saved = applicationRepository.save(application);

        // 6. Increment job counter
        job.setApplicationsCount(job.getApplicationsCount() + 1);
        jobRepository.save(job);

        return saved;
    }

    // ── Resume download — serves file bytes to recruiter ──────────────────────
    public byte[] getResumeBytes(String applicationId) throws Exception {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        if (app.getResumeUrl() == null)
            throw new RuntimeException("No resume attached to this application");
        Path path = Paths.get(app.getResumeUrl());
        if (!Files.exists(path))
            throw new RuntimeException("Resume file not found on server");
        return Files.readAllBytes(path);
    }

    public String getResumeName(String applicationId) {
        return applicationRepository.findById(applicationId)
            .map(Application::getResumeName)
            .orElse("resume.pdf");
    }

    public boolean hasResume(String applicationId) {
        return applicationRepository.findById(applicationId)
            .map(a -> a.getResumeUrl() != null && !a.getResumeUrl().isEmpty())
            .orElse(false);
    }

    // ── Fallback JSON apply (no file) ──────────────────────────────────────────
    public Application submitApplication(Application application) {
        if (applicationRepository.existsByUserIdAndJobId(
                application.getUserId(), application.getJobId()))
            throw new RuntimeException("You have already applied for this job");

        int matchScore = recommendationService.calculateMatchScore(application);
        application.setMatchScore(matchScore);

        Job job = jobRepository.findById(application.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setApplicationsCount(job.getApplicationsCount() + 1);
        jobRepository.save(job);

        return applicationRepository.save(application);
    }

    // ── Recruiter: enriched applicant profiles ─────────────────────────────────
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
            profile.setResumeName(app.getResumeName());
            profile.setHasResume(app.getResumeUrl() != null
                && !app.getResumeUrl().isEmpty());

            userRepository.findById(app.getUserId()).ifPresentOrElse(user -> {
                profile.setFirstName(user.getFirstName());
                profile.setLastName(user.getLastName());
                profile.setEmail(user.getEmail());
                profile.setSkills(user.getSkills());
                profile.setExperience(user.getExperience());
                profile.setEducation(user.getEducation());
                profile.setLinkedinUrl(user.getLinkedinUrl());
            }, () -> {
                String name = app.getCandidateName() != null
                    ? app.getCandidateName() : "";
                String[] parts = name.split(" ", 2);
                profile.setFirstName(parts.length > 0 ? parts[0] : "");
                profile.setLastName(parts.length > 1 ? parts[1] : "");
                profile.setEmail(app.getCandidateEmail());
            });

            profiles.add(profile);
        }
        return profiles;
    }

    // ── Common operations ──────────────────────────────────────────────────────

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
        List<Application> apps = applicationRepository.findByJobId(jobId);
        if ("matchScore".equals(sortBy)) {
            apps.sort(Comparator
                .comparingInt((Application a) ->
                    a.getMatchScore() != null ? a.getMatchScore() : 0)
                .reversed());
        } else if ("date".equals(sortBy)) {
            apps.sort(Comparator.comparing(Application::getAppliedAt).reversed());
        }
        return apps;
    }

    public List<Application> getJobApplications(String jobId) {
        return applicationRepository.findByJobIdOrderByMatchScoreDesc(jobId);
    }

    public List<Application> getJobApplicationsByStatus(String jobId, String status) {
        return applicationRepository.findByJobIdAndStatus(jobId, status);
    }

    public Application updateApplicationStatus(String id, String status) {
        Application app = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setStatus(status);
        app.setUpdatedAt(LocalDateTime.now());
        return applicationRepository.save(app);
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

    public Application updateRecruiterNotes(String applicationId, String notes) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setRecruiterNotes(notes);
        app.setUpdatedAt(LocalDateTime.now());
        return applicationRepository.save(app);
    }

    public List<Application> getAllApplicationsForRecruiter(String recruiterId) {
        List<Job> jobs = jobRepository.findByPostedBy(recruiterId);
        if (jobs.isEmpty()) return new ArrayList<>();
        List<String> jobIds = new ArrayList<>();
        for (Job job : jobs) jobIds.add(job.getId());
        return applicationRepository.findByJobIdIn(jobIds);
    }

    public CandidateProfile getCandidateFullProfile(String applicationId) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        User user = userRepository.findById(app.getUserId())
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
        profile.setResumeName(app.getResumeName());
        profile.setHasResume(app.getResumeUrl() != null);
        profile.setLinkedinUrl(user.getLinkedinUrl());
        profile.setApplicationId(app.getId());
        profile.setJobId(app.getJobId());
        profile.setStatus(app.getStatus());
        profile.setMatchScore(app.getMatchScore());
        profile.setCoverLetter(app.getCoverLetter());
        profile.setRecruiterNotes(app.getRecruiterNotes());
        profile.setAppliedAt(app.getAppliedAt());
        profile.setUpdatedAt(app.getUpdatedAt());
        return profile;
    }
}
