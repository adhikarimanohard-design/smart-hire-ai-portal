package com.smarthire.service;

import com.smarthire.config.JwtUtil;
import com.smarthire.dto.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.smarthire.model.User;
import com.smarthire.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
@Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User already exists with email: " + user.getEmail());
        }
        return userRepository.save(user);
    }
    
    public User updateUser(String id, User userDetails) {
User user = getUserById(id);
        
        user.setName(userDetails.getName());
        user.setPhone(userDetails.getPhone());
        user.setRole(userDetails.getRole());
        user.setSkills(userDetails.getSkills());
        user.setExperience(userDetails.getExperience());
        user.setEducation(userDetails.getEducation());
        user.setResumeUrl(userDetails.getResumeUrl());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    public void deleteUser(String id) {
        User user = getUserById(id);
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
public AuthResponse register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail());
        return new AuthResponse(token, saved.getEmail(), 
                saved.getFirstName(), saved.getLastName(), 
                saved.getId(), saved.getRole());
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(),
                user.getFirstName(), user.getLastName(),
                user.getId(), user.getRole());
    }

    public User uploadResume(String id, MultipartFile file) throws Exception {
        User user = getUserById(id);
        String uploadDir = "uploads/resumes/";
        Files.createDirectories(Paths.get(uploadDir));
        String fileName = id + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        Files.write(filePath, file.getBytes());
        user.setResumeUrl(filePath.toString());
        user.setResumeName(file.getOriginalFilename());
        user.setResumeUploaded(true);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}