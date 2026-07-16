package com.smarthire.service;

import com.smarthire.dto.RecruiterStats;
import com.smarthire.model.Application;
import com.smarthire.model.Job;
import com.smarthire.repository.ApplicationRepository;
import com.smarthire.repository.InterviewRepository;
import com.smarthire.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecruiterService {

    @Autowired private JobRepository jobRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private InterviewRepository interviewRepository;

    public RecruiterStats getDashboardStats(String recruiterId) {
        RecruiterStats stats = new RecruiterStats();

        stats.setActiveJobsCount(
            (int) jobRepository.countByPostedByAndActiveTrue(recruiterId));
        stats.setTotalJobsCount(
            (int) jobRepository.countByPostedBy(recruiterId));

        List<Job> recruiterJobs = jobRepository.findByPostedBy(recruiterId);

        if (!recruiterJobs.isEmpty()) {
            List<String> jobIds = new ArrayList<>();
            for (Job job : recruiterJobs) jobIds.add(job.getId());

            List<Application> allApplications =
                applicationRepository.findByJobIdIn(jobIds);

            stats.setTotalApplicants(allApplications.size());

            LocalDateTime startOfToday = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0);
            long todayCount = allApplications.stream()
                .filter(a -> a.getAppliedAt().isAfter(startOfToday))
                .count();
            stats.setNewApplicantsToday((int) todayCount);

            long shortlisted = allApplications.stream()
                .filter(a -> "SHORTLISTED".equals(a.getStatus()))
                .count();
            stats.setShortlistedCount((int) shortlisted);

            LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            long filled = allApplications.stream()
                .filter(a -> "ACCEPTED".equals(a.getStatus()))
                .filter(a -> a.getUpdatedAt().isAfter(startOfMonth))
                .count();
            stats.setPositionsFilledThisMonth((int) filled);

            double avgScore = allApplications.stream()
                .filter(a -> a.getMatchScore() != null)
                .mapToInt(Application::getMatchScore)
                .average().orElse(0.0);
            stats.setAverageMatchScore(
                Math.round(avgScore * 10.0) / 10.0);
        }

        stats.setInterviewsScheduled(
            (int) interviewRepository
                .countByRecruiterIdAndStatus(recruiterId, "SCHEDULED"));

        return stats;
    }
}
