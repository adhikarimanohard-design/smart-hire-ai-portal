
package com.smarthire.controller;

import com.smarthire.model.Interview;
import com.smarthire.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@CrossOrigin(origins = "*")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @PostMapping
    public ResponseEntity<?> scheduleInterview(
            @RequestBody Interview interview) {
        try {
            return ResponseEntity.ok(
                interviewService.scheduleInterview(interview));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<Interview>> getRecruiterInterviews(
            @PathVariable String recruiterId) {
        return ResponseEntity.ok(
            interviewService.getRecruiterInterviews(recruiterId));
    }

    @GetMapping("/recruiter/{recruiterId}/scheduled")
    public ResponseEntity<List<Interview>> getScheduledInterviews(
            @PathVariable String recruiterId) {
        return ResponseEntity.ok(
            interviewService.getScheduledInterviews(recruiterId));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Interview>> getCandidateInterviews(
            @PathVariable String candidateId) {
        return ResponseEntity.ok(
            interviewService.getCandidateInterviews(candidateId));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<Interview>> getInterviewsByApplication(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(
            interviewService.getInterviewsByApplication(applicationId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInterview(
            @PathVariable String id,
            @RequestBody Interview interview) {
        try {
            return ResponseEntity.ok(
                interviewService.updateInterview(id, interview));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/feedback")
    public ResponseEntity<?> addFeedback(
            @PathVariable String id,
            @RequestParam String feedback) {
        try {
            return ResponseEntity.ok(
                interviewService.addFeedback(id, feedback));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelInterview(@PathVariable String id) {
        try {
            return ResponseEntity.ok(
                interviewService.cancelInterview(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
