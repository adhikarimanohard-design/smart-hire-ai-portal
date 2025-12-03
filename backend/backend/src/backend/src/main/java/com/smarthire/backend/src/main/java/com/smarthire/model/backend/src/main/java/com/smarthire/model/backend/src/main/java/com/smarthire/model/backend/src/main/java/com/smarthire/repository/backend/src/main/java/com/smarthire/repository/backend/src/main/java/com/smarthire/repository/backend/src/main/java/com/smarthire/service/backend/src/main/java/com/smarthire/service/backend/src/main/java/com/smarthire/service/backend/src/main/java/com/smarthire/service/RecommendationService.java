package com.smarthire.service;

import com.smarthire.model.Application;
import com.smarthire.model.Job;
import com.smarthire.model.User;
import com.smarthire.repository.JobRepository;
import com.smarthire.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JobRepository jobRepository;
    
    public int calculateMatchScore(Application application) {
        // Simple demo calculation
        // In production, this would call ML engine
        return (int) (Math.random() * (95 - 70) + 70);
    }
    
    public int calculateMatchScore(User user, Job job) {
        if (user.getSkills() == null || job.getSkills() == null) {
            return 50;
        }
        
        // Calculate skill overlap
        List<String> userSkills = user.getSkills().stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        
        List<String> jobSkills = job.getSkills().stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        
        long matchingSkills = userSkills.stream()
            .filter(jobSkills::contains)
            .count();
        
        double skillMatchRatio = (double) matchingSkills / jobSkills.size();
        
        // Skill score (70% weight)
        int skillScore = (int) (skillMatchRatio * 70);
        
        // Experience score (20% weight) - simplified
        int experienceScore = 20;
        
        // Bonus score (10% weight)
        int bonusScore = 10;
        
        return Math.min(skillScore + experienceScore + bonusScore, 100);
    }
    
    public List<Job> getRecommendedJobs(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Job> allJobs = jobRepository.findByActiveTrue();
        
        // Sort jobs by match score
        return allJobs.stream()
            .sorted((j1, j2) -> {
                int score1 = calculateMatchScore(user, j1);
                int score2 = calculateMatchScore(user, j2);
                return Integer.compare(score2, score1);
            })
            .limit(10)
            .collect(Collectors.toList());
    }
}
