package com.smarthire.dto;

import java.util.List;

public class BulkStatusRequest {

    private List<String> applicationIds;
    private String status;

    public BulkStatusRequest() {}

    public List<String> getApplicationIds() { return applicationIds; }
    public void setApplicationIds(List<String> applicationIds) {
        this.applicationIds = applicationIds;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
