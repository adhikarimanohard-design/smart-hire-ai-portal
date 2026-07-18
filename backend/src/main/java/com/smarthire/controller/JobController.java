
package com.smarthire.controller;

import com.smarthire.dto.CandidateProfile;
import com.smarthire.model.Job;
import com.smarthire.service.ApplicationService;
import com.smarthire.service.JobService;
import com.smarthire.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    @Autowired private JobService jobService;
    @Autowired private RecommendationService recommendationService;
    @Autowired private ApplicationService applicationService;

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllActiveJobs());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Job>> searchJobs(
            @RequestParam String query) {
        return ResponseEntity.ok(jobService.searchJobs(query));
    }

    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<List<Job>> getRecommendations(
            @PathVariable String userId) {
        return ResponseEntity.ok(
            recommendationService.getRecommendedJobs(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable String id) {
        try {
            jobService.incrementViewCount(id);
            return ResponseEntity.ok(jobService.getJobById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody Job job) {
        try {
            return ResponseEntity.ok(jobService.createJob(job));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<Job>> getRecruiterJobs(
            @PathVariable String recruiterId) {
        return ResponseEntity.ok(
            jobService.getJobsByRecruiter(recruiterId));
    }

    @GetMapping("/recruiter/{recruiterId}/active")
    public ResponseEntity<List<Job>> getActiveRecruiterJobs(
            @PathVariable String recruiterId) {
        return ResponseEntity.ok(
            jobService.getActiveJobsByRecruiter(recruiterId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(
            @PathVariable String id, @RequestBody Job job) {
        try {
            return ResponseEntity.ok(jobService.updateJob(id, job));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable String id) {
        try {
            jobService.deleteJob(id);
            return ResponseEntity.ok("Job closed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentlyDeleteJob(@PathVariable String id) {
        try {
            jobService.permanentlyDeleteJob(id);
            return ResponseEntity.ok("Job deleted permanently");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/reopen")
    public ResponseEntity<?> reopenJob(@PathVariable String id) {
        try {
            return ResponseEntity.ok(jobService.reopenJob(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<?> applyToJob(
            @PathVariable String id,
            @RequestParam("userId") String userId,
            @RequestParam(value = "resume", required = false) MultipartFile resume,
            @RequestParam(value = "coverLetter", required = false) String coverLetter) {
        log.info("APPLY REQUEST RECEIVED: jobId={}, userId={}, resumePresent={}, resumeSize={}",
            id, userId, resume != null, resume != null ? resume.getSize() : 0);
        try {
            var result = applicationService.applyToJob(id, userId, resume, coverLetter);
            log.info("APPLY SUCCEEDED: jobId={}, userId={}, applicationId={}",
                id, userId, result.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("APPLY FAILED: jobId={}, userId={}, error={}", id, userId, e.toString(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/applicants")
    public ResponseEntity<List<CandidateProfile>> getJobApplicants(
            @PathVariable String id) {
        return ResponseEntity.ok(applicationService.getJobApplicantsProfiles(id));
    }
}
