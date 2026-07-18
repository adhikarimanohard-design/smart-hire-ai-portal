package com.smarthire.controller;

import com.smarthire.dto.RecruiterStats;
import com.smarthire.model.Job;
import com.smarthire.service.RecruiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter")
public class RecruiterController {

    @Autowired
    private RecruiterService recruiterService;

    @GetMapping("/{recruiterId}/stats")
    public ResponseEntity<?> getDashboardStats(@PathVariable String recruiterId) {
        try {
            return ResponseEntity.ok(recruiterService.getDashboardStats(recruiterId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
