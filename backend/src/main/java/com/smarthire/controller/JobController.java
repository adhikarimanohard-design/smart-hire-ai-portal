package com.smarthire.controller;

import com.smarthire.model.Job;
import com.smarthire.service.JobService;
import com.smarthire.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired private JobService jobService;
    @Autowired private RecommendationService recommendationService;

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

    @PutMapping("/{id}/reopen")
    public ResponseEntity<?> reopenJob(@PathVariable String id) {
        try {
            return ResponseEntity.ok(jobService.reopenJob(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
