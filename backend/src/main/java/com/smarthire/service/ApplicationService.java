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

    // ── Primary apply: resume extracted from local storage, then persisted
    //    directly into MongoDB (not left on disk) ────────────────────────────
    /**
     * RESUME PERSISTENCE:
     *
     * Multipart uploads land in local temp storage first (that's how
     * Spring/Tomcat receives them). We extract the bytes from there and
     * write them straight into the Application document in MongoDB, then
     * discard the temp file — nothing resume-related is left on disk.
     *
     * Why: Render's free-tier filesystem is ephemeral. Anything written to
     * "uploads/resumes/" (the old approach) is wiped on the next restart,
     * spin-down, or redeploy, so recruiters would eventually get
     * "resume not found" even though the application itself saved fine.
     * Storing the bytes in MongoDB means the resume survives the server's
     * lifecycle. Files are capped at 5MB (application.properties), well
     * under MongoDB's 16MB per-document limit, so this stays fast.
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

        // 3. Extract resume from local (temp) storage, then store the
        //    bytes directly in MongoDB — nothing persists on local disk
        if (resume != null && !resume.isEmpty()) {
            String safeOriginal = resume.getOriginalFilename()
                .replaceAll("[^a-zA-Z0-9._-]", "_");

            // Land the upload in a temp file first (extract from local
            // storage)...
            Path tempFile = Files.createTempFile("resume_", "_" + safeOriginal);
            try {
                resume.transferTo(tempFile);
                byte[] fileBytes = Files.readAllBytes(tempFile);

                // ...then persist the bytes into the MongoDB document.
                application.setResumeData(fileBytes);
                application.setResumeContentType(resume.getContentType());
                application.setResumeName(resume.getOriginalFilename());
            } finally {
                // Temp file's job is done — don't rely on it surviving.
                Files.deleteIfExists(tempFile);
            }

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

    // ── Resume download — serves bytes straight from MongoDB ──────────────────
    public byte[] getResumeBytes(String applicationId) throws Exception {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
        if (app.getResumeData() == null || app.getResumeData().length == 0)
            throw new RuntimeException("No resume attached to this application");
        return app.getResumeData();
    }

    public String getResumeName(String applicationId) {
        return applicationRepository.findById(applicationId)
            .map(Application::getResumeName)
            .orElse("resume.pdf");
    }

    public String getResumeContentType(String applicationId) {
        return applicationRepository.findById(applicationId)
            .map(Application::getResumeContentType)
            .orElse("application/octet-stream");
    }

    public boolean hasResume(String applicationId) {
        return applicationRepository.findById(applicationId)
            .map(a -> a.getResumeData() != null && a.getResumeData().length > 0)
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
            profile.setHasResume(app.getResumeData() != null
                && app.getResumeData().length > 0);

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
        profile.setHasResume(app.getResumeData() != null && app.getResumeData().length > 0);
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
