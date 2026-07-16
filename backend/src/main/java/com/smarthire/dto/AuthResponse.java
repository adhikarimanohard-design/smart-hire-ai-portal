package com.smarthire.dto;

public class AuthResponse {

    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private String userId;
    private String role;

    // Extra for recruiters — null for candidates
    private String companyName;
    private String companyLogoUrl;

    // Candidate constructor
    public AuthResponse(String token, String email, String firstName,
                        String lastName, String userId, String role) {
        this.token = token;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userId = userId;
        this.role = role;
    }

    // Recruiter constructor — includes company info
    public AuthResponse(String token, String email, String firstName,
                        String lastName, String userId, String role,
                        String companyName, String companyLogoUrl) {
        this(token, email, firstName, lastName, userId, role);
        this.companyName = companyName;
        this.companyLogoUrl = companyLogoUrl;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyLogoUrl() { return companyLogoUrl; }
    public void setCompanyLogoUrl(String companyLogoUrl) { this.companyLogoUrl = companyLogoUrl; }
}
