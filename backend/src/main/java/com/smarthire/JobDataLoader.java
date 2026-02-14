package com.smarthire;

import com.smarthire.model.Job;
import com.smarthire.repository.JobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class JobDataLoader implements CommandLineRunner {

    private final JobRepository jobRepository;

    public JobDataLoader(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only load data if the database is empty
        if (jobRepository.count() == 0) {

            // Job 1: Senior Full Stack Engineer
            Job job1 = new Job();
            job1.setTitle("Senior Full Stack Engineer");
            job1.setCompany("TechCorp Inc");
            job1.setLocation("Bangalore, India");
            job1.setType("Full-time");
            job1.setSalaryRange("₹50-70 LPA");
            job1.setDescription("Leading tech company seeking experienced full stack engineer to build scalable cloud applications.");
            job1.setSkills(Arrays.asList("React", "Node.js", "AWS", "MongoDB", "TypeScript"));
            job1.setRequirements(Arrays.asList("5+ years experience", "B.Tech/M.Tech in CS", "Experience with Microservices"));
            job1.setPostedBy("HR Team");
            job1.setExpiryDate(LocalDateTime.now().plusDays(30)); // Expires in 30 days
            job1.setApplicationsCount(120);
            job1.setViewsCount(450);

            // Job 2: ML Engineer
            Job job2 = new Job();
            job2.setTitle("ML Engineer");
            job2.setCompany("AI Innovations");
            job2.setLocation("Remote");
            job2.setType("Contract");
            job2.setSalaryRange("₹60-80 LPA");
            job2.setDescription("Build cutting-edge ML models for recommendation systems and natural language processing.");
            job2.setSkills(Arrays.asList("Python", "TensorFlow", "PyTorch", "NLP", "AWS"));
            job2.setRequirements(Arrays.asList("Strong Math background", "Experience with LLMs", "Published research papers is a plus"));
            job2.setPostedBy("Tech Lead");
            job2.setExpiryDate(LocalDateTime.now().plusDays(20));
            job2.setApplicationsCount(85);
            job2.setViewsCount(320);

            // Job 3: DevOps Architect
            Job job3 = new Job();
            job3.setTitle("DevOps Architect");
            job3.setCompany("CloudScale Systems");
            job3.setLocation("Hyderabad, India");
            job3.setType("Full-time");
            job3.setSalaryRange("₹55-75 LPA");
            job3.setDescription("Design and implement scalable cloud infrastructure for high-traffic applications.");
            job3.setSkills(Arrays.asList("Kubernetes", "Docker", "AWS", "CI/CD", "Terraform"));
            job3.setRequirements(Arrays.asList("Certified AWS Architect", "Experience in FinTech", "Knowledge of security best practices"));
            job3.setPostedBy("Recruiting Manager");
            job3.setExpiryDate(LocalDateTime.now().plusDays(45));
            job3.setApplicationsCount(40);
            job3.setViewsCount(150);

            // Save all to MongoDB
            jobRepository.saveAll(List.of(job1, job2, job3));

            System.out.println("✅ JOBS ADDED TO MONGODB SUCCESSFULLY");
        }
    }
}
