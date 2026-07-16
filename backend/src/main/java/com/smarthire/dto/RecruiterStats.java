package com.smarthire.dto;

public class RecruiterStats {

    private int activeJobsCount;
    private int totalJobsCount;
    private int totalApplicants;
    private int newApplicantsToday;
    private int shortlistedCount;
    private int interviewsScheduled;
    private int positionsFilledThisMonth;
    private double averageMatchScore;

    public RecruiterStats() {}

    public int getActiveJobsCount() { return activeJobsCount; }
    public void setActiveJobsCount(int v) { this.activeJobsCount = v; }

    public int getTotalJobsCount() { return totalJobsCount; }
    public void setTotalJobsCount(int v) { this.totalJobsCount = v; }

    public int getTotalApplicants() { return totalApplicants; }
    public void setTotalApplicants(int v) { this.totalApplicants = v; }

    public int getNewApplicantsToday() { return newApplicantsToday; }
    public void setNewApplicantsToday(int v) { this.newApplicantsToday = v; }

    public int getShortlistedCount() { return shortlistedCount; }
    public void setShortlistedCount(int v) { this.shortlistedCount = v; }

    public int getInterviewsScheduled() { return interviewsScheduled; }
    public void setInterviewsScheduled(int v) { this.interviewsScheduled = v; }

    public int getPositionsFilledThisMonth() { return positionsFilledThisMonth; }
    public void setPositionsFilledThisMonth(int v) { this.positionsFilledThisMonth = v; }

    public double getAverageMatchScore() { return averageMatchScore; }
    public void setAverageMatchScore(double v) { this.averageMatchScore = v; }
}
