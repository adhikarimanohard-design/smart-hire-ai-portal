package com.smarthire.service;

import com.smarthire.config.JwtUtil;
import com.smarthire.dto.AuthResponse;
import com.smarthire.model.User;
import com.smarthire.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(
                "User not found with email: " + email));
    }

    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail()))
            throw new RuntimeException("Email already exists");
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        User user = getUserById(id);
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public AuthResponse register(User user) {
        if (userRepository.existsByEmail(user.getEmail()))
            throw new RuntimeException("Email already registered");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail());

        if ("EMPLOYER".equals(saved.getRole())) {
            return new AuthResponse(token, saved.getEmail(),
                saved.getFirstName(), saved.getLastName(),
                saved.getId(), saved.getRole(),
                saved.getCompanyName(), saved.getCompanyLogoUrl());
        }
        return new AuthResponse(token, saved.getEmail(),
            saved.getFirstName(), saved.getLastName(),
            saved.getId(), saved.getRole());
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(
                "No account found with this email"));
        if (!user.isActive())
            throw new RuntimeException("Account has been deactivated");
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new RuntimeException("Incorrect password");

        String token = jwtUtil.generateToken(user.getEmail());
        if ("EMPLOYER".equals(user.getRole())) {
            return new AuthResponse(token, user.getEmail(),
                user.getFirstName(), user.getLastName(),
                user.getId(), user.getRole(),
                user.getCompanyName(), user.getCompanyLogoUrl());
        }
        return new AuthResponse(token, user.getEmail(),
            user.getFirstName(), user.getLastName(),
            user.getId(), user.getRole());
    }

    public User updateUser(String id, User userDetails) {
        User user = getUserById(id);
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setName(userDetails.getName());
        user.setPhone(userDetails.getPhone());
        user.setSkills(userDetails.getSkills());
        user.setExperience(userDetails.getExperience());
        user.setEducation(userDetails.getEducation());
        user.setLinkedinUrl(userDetails.getLinkedinUrl());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
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

    public User updateRecruiterProfile(String id, User userDetails) {
        User user = getUserById(id);
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setPhone(userDetails.getPhone());
        user.setJobTitle(userDetails.getJobTitle());
        user.setLinkedinUrl(userDetails.getLinkedinUrl());
        user.setCompanyName(userDetails.getCompanyName());
        user.setCompanyWebsite(userDetails.getCompanyWebsite());
        user.setCompanySize(userDetails.getCompanySize());
        user.setCompanyIndustry(userDetails.getCompanyIndustry());
        user.setCompanyDescription(userDetails.getCompanyDescription());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User uploadCompanyLogo(String id, MultipartFile file) throws Exception {
        User user = getUserById(id);
        String uploadDir = "uploads/logos/";
        Files.createDirectories(Paths.get(uploadDir));
        String fileName = id + "_logo_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        Files.write(filePath, file.getBytes());
        user.setCompanyLogoUrl(filePath.toString());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public List<User> searchCandidatesBySkill(String skill) {
        return userRepository.findBySkillsContainingIgnoreCase(skill)
            .stream()
            .filter(u -> "CANDIDATE".equals(u.getRole()))
            .filter(User::isActive)
            .collect(Collectors.toList());
    }

    public List<User> getAllCandidates() {
        return userRepository.findByRole("CANDIDATE")
            .stream()
            .filter(User::isActive)
            .collect(Collectors.toList());
    }
}
