package com.smarthire.service;

import com.smarthire.dto.RecruiterStats;
import com.smarthire.model.Application;
import com.smarthire.model.Job;
import com.smarthire.repository.ApplicationRepository;
import com.smarthire.repository.InterviewRepository;
import com.smarthire.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecruiterService {

    @Autowired private JobRepository jobRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private InterviewRepository interviewRepository;

    /**
     * Jobs posted by this recruiter, enriched with a live applicant count
     * so the recruiter dashboard can show "N applicants" per listing.
     */
    public List<Job> getJobsByRecruiter(String recruiterId) {
        List<Job> jobs = jobRepository.findByPostedByOrderByPostedDateDesc(recruiterId);
        for (Job job : jobs) {
            long count = applicationRepository.countByJobId(job.getId());
            job.setApplicantCount((int) count);
        }
        return jobs;
    }

    public RecruiterStats getDashboardStats(String recruiterId) {
        RecruiterStats stats = new RecruiterStats();

        stats.setActiveJobs(
            (int) jobRepository.countByPostedByAndActiveTrue(recruiterId));
        stats.setTotalJobs(
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

            List<Application> hired = allApplications.stream()
                .filter(a -> "HIRED".equals(a.getStatus()))
                .collect(Collectors.toList());
            stats.setTotalHires(hired.size());

            double avgDays = hired.stream()
                .filter(a -> a.getAppliedAt() != null && a.getUpdatedAt() != null)
                .mapToLong(a -> Duration.between(a.getAppliedAt(), a.getUpdatedAt()).toDays())
                .average().orElse(0.0);
            stats.setAvgTimeToHire(Math.round(avgDays * 10.0) / 10.0);

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
