package com.smarthire.controller;

import com.smarthire.dto.BulkStatusRequest;
import com.smarthire.dto.CandidateProfile;
import com.smarthire.model.Application;
import com.smarthire.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    // ── Fallback JSON apply (no file) ──────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> submitApplication(@RequestBody Application application) {
        try {
            return ResponseEntity.ok(applicationService.submitApplication(application));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Candidate: my applications ─────────────────────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Application>> getUserApplications(
            @PathVariable String userId) {
        return ResponseEntity.ok(applicationService.getUserApplications(userId));
    }

    // ── Recruiter: all applicants for a job ────────────────────────────────────
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Application>> getJobApplications(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "matchScore") String sortBy) {
        return ResponseEntity.ok(
            applicationService.getJobApplicationsSorted(jobId, sortBy));
    }

    // ── Recruiter: filter by status ────────────────────────────────────────────
    @GetMapping("/job/{jobId}/status")
    public ResponseEntity<List<Application>> getJobApplicationsByStatus(
            @PathVariable String jobId,
            @RequestParam String status) {
        return ResponseEntity.ok(
            applicationService.getJobApplicationsByStatus(jobId, status));
    }

    // ── Recruiter: full candidate profile card ─────────────────────────────────
    @GetMapping("/{applicationId}/profile")
    public ResponseEntity<?> getCandidateProfile(
            @PathVariable String applicationId) {
        try {
            return ResponseEntity.ok(
                applicationService.getCandidateFullProfile(applicationId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * RESUME DOWNLOAD — reads bytes straight out of the MongoDB document
     * and streams them to the recruiter. Nothing is read from local disk,
     * so this keeps working across restarts, spin-downs, and redeploys.
     */
    @GetMapping("/{applicationId}/resume")
    public ResponseEntity<?> downloadResume(@PathVariable String applicationId) {
        try {
            byte[] bytes       = applicationService.getResumeBytes(applicationId);
            String fileName    = applicationService.getResumeName(applicationId);
            String contentType = applicationService.getResumeContentType(applicationId);

            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(contentType);
            } catch (Exception parseErr) {
                mediaType = fileName.toLowerCase().endsWith(".pdf")
                    ? MediaType.APPLICATION_PDF
                    : MediaType.APPLICATION_OCTET_STREAM;
            }

            return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileName + "\"")
                .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Recruiter: update status (PATCH) ───────────────────────────────────────
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(
                applicationService.updateApplicationStatus(id, body.get("status")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Recruiter: bulk status update ──────────────────────────────────────────
    @PutMapping("/bulk-status")
    public ResponseEntity<?> bulkUpdateStatus(@RequestBody BulkStatusRequest request) {
        try {
            return ResponseEntity.ok(applicationService.bulkUpdateStatus(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Recruiter: notes ───────────────────────────────────────────────────────
    @PutMapping("/{applicationId}/notes")
    public ResponseEntity<?> updateNotes(
            @PathVariable String applicationId,
            @RequestParam String notes) {
        try {
            return ResponseEntity.ok(
                applicationService.updateRecruiterNotes(applicationId, notes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Admin ──────────────────────────────────────────────────────────────────
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Application>> getApplicationsByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(applicationService.getApplicationsByStatus(status));
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<Application>> getAllRecruiterApplications(
            @PathVariable String recruiterId) {
        return ResponseEntity.ok(
            applicationService.getAllApplicationsForRecruiter(recruiterId));
    }
}
