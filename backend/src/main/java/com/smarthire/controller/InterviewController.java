package com.smarthire.controller;

import com.smarthire.model.Interview;
import com.smarthire.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @PostMapping
    public ResponseEntity<?> scheduleInterview(
            @RequestBody Interview interview) {
        try {
            return ResponseEntity.ok(interviewService.scheduleInterview(interview));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Interview>> getUserInterviews(
            @PathVariable String userId) {
        return ResponseEntity.ok(interviewService.getUserInterviews(userId));
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<Interview>> getRecruiterInterviews(
            @PathVariable String recruiterId) {
        return ResponseEntity.ok(interviewService.getRecruiterInterviews(recruiterId));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<?> getInterviewByApplication(
            @PathVariable String applicationId) {
        try {
            return ResponseEntity.ok(
                interviewService.getInterviewByApplication(applicationId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInterview(
            @PathVariable String id, @RequestBody Interview interview) {
        try {
            return ResponseEntity.ok(interviewService.updateInterview(id, interview));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id, @RequestParam String status) {
        try {
            return ResponseEntity.ok(interviewService.updateStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelInterview(@PathVariable String id) {
        try {
            interviewService.cancelInterview(id);
            return ResponseEntity.ok("Interview cancelled");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
