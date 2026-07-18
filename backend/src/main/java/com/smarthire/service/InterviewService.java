package com.smarthire.service;

import com.smarthire.model.Application;
import com.smarthire.model.Interview;
import com.smarthire.model.Job;
import com.smarthire.model.User;
import com.smarthire.repository.ApplicationRepository;
import com.smarthire.repository.InterviewRepository;
import com.smarthire.repository.JobRepository;
import com.smarthire.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewService {

    @Autowired private InterviewRepository interviewRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JobRepository jobRepository;

    public Interview scheduleInterview(Interview interview) {
        Application application = applicationRepository
            .findById(interview.getApplicationId())
            .orElseThrow(() -> new RuntimeException("Application not found"));

        User candidate = userRepository
            .findById(application.getUserId())
            .orElseThrow(() -> new RuntimeException("Candidate not found"));

        Job job = jobRepository
            .findById(application.getJobId())
            .orElseThrow(() -> new RuntimeException("Job not found"));

        interview.setCandidateId(candidate.getId());
        interview.setJobId(job.getId());
        interview.setJobTitle(job.getTitle());
        interview.setCandidateName(
            candidate.getFirstName() + " " + candidate.getLastName());
        interview.setCandidateEmail(candidate.getEmail());

        application.setStatus("INTERVIEW");
        application.setUpdatedAt(LocalDateTime.now());
        applicationRepository.save(application);

        return interviewRepository.save(interview);
    }

    public List<Interview> getRecruiterInterviews(String recruiterId) {
        return interviewRepository
            .findByRecruiterIdOrderByScheduledAtAsc(recruiterId);
    }

    public List<Interview> getScheduledInterviews(String recruiterId) {
        return interviewRepository
            .findByRecruiterIdAndStatus(recruiterId, "SCHEDULED");
    }

    public List<Interview> getCandidateInterviews(String candidateId) {
        return interviewRepository.findByCandidateId(candidateId);
    }

    public List<Interview> getInterviewsByApplication(String applicationId) {
        return interviewRepository.findByApplicationId(applicationId);
    }

    public Interview updateInterview(String id, Interview interviewDetails) {
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Interview not found"));
        interview.setScheduledAt(interviewDetails.getScheduledAt());
        interview.setDurationMinutes(interviewDetails.getDurationMinutes());
        interview.setType(interviewDetails.getType());
        interview.setMeetingLink(interviewDetails.getMeetingLink());
        interview.setLocation(interviewDetails.getLocation());
        interview.setNotes(interviewDetails.getNotes());
        interview.setStatus(interviewDetails.getStatus());
        interview.setUpdatedAt(LocalDateTime.now());
        return interviewRepository.save(interview);
    }

    public Interview addFeedback(String id, String feedback) {
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Interview not found"));
        interview.setFeedback(feedback);
        interview.setStatus("COMPLETED");
        interview.setUpdatedAt(LocalDateTime.now());
        return interviewRepository.save(interview);
    }

    public Interview cancelInterview(String id) {
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Interview not found"));
        interview.setStatus("CANCELLED");
        interview.setUpdatedAt(LocalDateTime.now());

        applicationRepository.findById(interview.getApplicationId())
            .ifPresent(app -> {
                app.setStatus("SHORTLISTED");
                app.setUpdatedAt(LocalDateTime.now());
                applicationRepository.save(app);
            });

        return interviewRepository.save(interview);
    }

    public long countScheduledInterviews(String recruiterId) {
        return interviewRepository
            .countByRecruiterIdAndStatus(recruiterId, "SCHEDULED");
    }
}