package com.smarthire.controller;

import com.smarthire.dto.RecruiterStats;
import com.smarthire.service.RecruiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recruiter")
@CrossOrigin(origins = "*")
public class RecruiterController {

    @Autowired
    private RecruiterService recruiterService;

    @GetMapping("/{recruiterId}/stats")
    public ResponseEntity<?> getDashboardStats(
            @PathVariable String recruiterId) {
        try {
            RecruiterStats stats =
                recruiterService.getDashboardStats(recruiterId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
