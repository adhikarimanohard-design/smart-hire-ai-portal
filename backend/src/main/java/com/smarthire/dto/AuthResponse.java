package com.smarthire.dto;

public class AuthResponse {

    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private String userId;
    private String role;

    // Constructor
    public AuthResponse(String token, String email, String firstName, 
                        String lastName, String userId, String role) {
        this.token = token;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userId = userId;
        this.role = role;
    }

    // Getters
    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUserId() { return userId; }
    public String getRole() { return role; }

    // Setters
    public void setToken(String token) { this.token = token; }
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setRole(String role) { this.role = role; }
}