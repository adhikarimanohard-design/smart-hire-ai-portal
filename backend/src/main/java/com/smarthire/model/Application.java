package com.smarthire.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "applications")
public class Application {

    @Id
    private String id;

    private String jobId;
    private String userId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String coverLetter;

    // Resume is extracted from local (temp) storage during upload, then
    // persisted straight into this MongoDB document as raw bytes — NOT
    // as a disk path. Render's free-tier filesystem is ephemeral, so any
    // file left on disk is wiped on the next restart/redeploy; storing
    // the bytes here means the resume survives regardless of the server
    // lifecycle. Files are capped at 5MB (see application.properties),
    // well under MongoDB's 16MB document limit.
    private byte[] resumeData;        // raw file bytes
    private String resumeContentType; // e.g. "application/pdf"
    private String resumeName;        // original filename shown to recruiter

    // PENDING → SHORTLISTED → INTERVIEWING → HIRED / REJECTED
    private String status;

    private Integer matchScore;
    private String recruiterNotes;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    public Application() {
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status    = "PENDING";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getCandidatePhone() { return candidatePhone; }
    public void setCandidatePhone(String candidatePhone) { this.candidatePhone = candidatePhone; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public byte[] getResumeData() { return resumeData; }
    public void setResumeData(byte[] resumeData) { this.resumeData = resumeData; }

    public String getResumeContentType() { return resumeContentType; }
    public void setResumeContentType(String resumeContentType) { this.resumeContentType = resumeContentType; }

    public String getResumeName() { return resumeName; }
    public void setResumeName(String resumeName) { this.resumeName = resumeName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getMatchScore() { return matchScore; }
    public void setMatchScore(Integer matchScore) { this.matchScore = matchScore; }

    public String getRecruiterNotes() { return recruiterNotes; }
    public void setRecruiterNotes(String notes) { this.recruiterNotes = notes; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
