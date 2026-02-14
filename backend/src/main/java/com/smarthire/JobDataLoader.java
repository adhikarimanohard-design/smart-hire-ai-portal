package com.smarthire;

import com.smarthire.model.Job;
import com.smarthire.model.User;
import com.smarthire.repository.JobRepository;
import com.smarthire.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class JobDataLoader implements CommandLineRunner {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    // ✅ Inject BOTH repositories here
    public JobDataLoader(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadJobs();
        loadUsers();
    }

    private void loadJobs() {
        if (jobRepository.count() == 0) {
            Job job1 = new Job();
            job1.setTitle("Senior Full Stack Engineer");
            job1.setCompany("TechCorp Inc");
            job1.setLocation("Bangalore, India");
            job1.setType("Full-time");
            job1.setSalaryRange("₹50-70 LPA");
            job1.setDescription("Leading tech company seeking experienced full stack engineer.");
            job1.setSkills(Arrays.asList("React", "Node.js", "AWS", "MongoDB", "TypeScript"));
            job1.setRequirements(Arrays.asList("5+ years experience", "B.Tech/M.Tech in CS"));
            job1.setPostedBy("HR Team");
            job1.setExpiryDate(LocalDateTime.now().plusDays(30));
            job1.setApplicationsCount(120);
            job1.setViewsCount(450);

            // Add more jobs if you like...
            jobRepository.save(job1);
            System.out.println("✅ JOBS ADDED TO MONGODB");
        }
    }

    private void loadUsers() {
        // ✅ Only add users if the DB is empty
        if (userRepository.count() == 0) {
            
            // User 1: Candidate (You!)
            User user1 = new User();
            user1.setName("Adhikari Manohar");
            user1.setEmail("manohar@example.com");
            user1.setPhone("+91-9876543210");
            user1.setRole("Candidate");
            user1.setSkills(Arrays.asList("Java", "Spring Boot", "React", "MongoDB"));
            user1.setExperience("Fresher");
            user1.setEducation("B.Tech Computer Science");
            user1.setMatchScore(85);
            user1.setActive(true);
            user1.setCreatedAt(LocalDateTime.now());
            user1.setUpdatedAt(LocalDateTime.now());

            // User 2: Recruiter
            User user2 = new User();
            user2.setName("Sarah Recruiter");
            user2.setEmail("sarah@techcorp.com");
            user2.setPhone("+91-9876543211");
            user2.setRole("Recruiter");
            user2.setActive(true);
            user2.setCreatedAt(LocalDateTime.now());

            userRepository.saveAll(Arrays.asList(user1, user2));
            System.out.println("✅ USERS ADDED TO MONGODB SUCCESSFULLY");
        } else {
            System.out.println("ℹ️ Users already exist. Skipping.");
        }
    }
}
