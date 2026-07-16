package com.smarthire.dto;

public class RecruiterStats {

    private int activeJobs;
    private int totalJobs;
    private int totalApplicants;
    private int newApplicantsToday;
    private int shortlistedCount;
    private int interviewsScheduled;
    private int totalHires;
    private double avgTimeToHire;
    private double averageMatchScore;

    public RecruiterStats() {}

    public int getActiveJobs() { return activeJobs; }
    public void setActiveJobs(int v) { this.activeJobs = v; }

    public int getTotalJobs() { return totalJobs; }
    public void setTotalJobs(int v) { this.totalJobs = v; }

    public int getTotalApplicants() { return totalApplicants; }
    public void setTotalApplicants(int v) { this.totalApplicants = v; }

    public int getNewApplicantsToday() { return newApplicantsToday; }
    public void setNewApplicantsToday(int v) { this.newApplicantsToday = v; }

    public int getShortlistedCount() { return shortlistedCount; }
    public void setShortlistedCount(int v) { this.shortlistedCount = v; }

    public int getInterviewsScheduled() { return interviewsScheduled; }
    public void setInterviewsScheduled(int v) { this.interviewsScheduled = v; }

    public int getTotalHires() { return totalHires; }
    public void setTotalHires(int v) { this.totalHires = v; }

    public double getAvgTimeToHire() { return avgTimeToHire; }
    public void setAvgTimeToHire(double v) { this.avgTimeToHire = v; }

    public double getAverageMatchScore() { return averageMatchScore; }
    public void setAverageMatchScore(double v) { this.averageMatchScore = v; }
}
