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

    public JobDataLoader(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadUsers();
        loadJobs();
    }

    private void loadUsers() {
        if (userRepository.count() == 0) {
            
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

            User user2 = new User();
            user2.setName("Sarah Recruiter");
            user2.setEmail("sarah@techcorp.com");
            user2.setPhone("+91-9876543211");
            user2.setRole("Recruiter");
            user2.setCompany("TechCorp Inc");
            user2.setActive(true);
            user2.setCreatedAt(LocalDateTime.now());
            user2.setUpdatedAt(LocalDateTime.now());

            User user3 = new User();
            user3.setName("David HiringManager");
            user3.setEmail("david@ai-innovations.com");
            user3.setPhone("+91-9876543212");
            user3.setRole("Recruiter");
            user3.setCompany("AI Innovations");
            user3.setActive(true);
            user3.setCreatedAt(LocalDateTime.now());
            user3.setUpdatedAt(LocalDateTime.now());

            userRepository.saveAll(Arrays.asList(user1, user2, user3));
            System.out.println("✅ USERS ADDED TO MONGODB SUCCESSFULLY");
        } else {
            System.out.println("ℹ️ Users already exist in database. Skipping.");
        }
    }

    private void loadJobs() {
        if (jobRepository.count() > 0) {
            System.out.println("📊 Database already has " + jobRepository.count() + " jobs. Skipping seeding.");
            return;
        }
        
        System.out.println("🌱 Seeding database with jobs...");

        List<User> recruiters = userRepository.findAll().stream()
                .filter(u -> "Recruiter".equalsIgnoreCase(u.getRole()))
                .toList();

        String techCorpRecruiterId = recruiters.stream()
                .filter(r -> "TechCorp Inc".equals(r.getCompany()))
                .findFirst()
                .map(User::getId)
                .orElse("default-recruiter-1");

        String aiInnovationsRecruiterId = recruiters.stream()
                .filter(r -> "AI Innovations".equals(r.getCompany()))
                .findFirst()
                .map(User::getId)
                .orElse("default-recruiter-2");

        Job job1 = new Job();
        job1.setTitle("Senior Full Stack Engineer");
        job1.setCompany("TechCorp Inc");
        job1.setLocation("Bangalore, India");
        job1.setType("Full-time");
        job1.setSalaryRange("₹50-70 LPA");
        job1.setDescription("Leading tech company seeking experienced full stack engineer to build scalable cloud applications.");
        job1.setSkills(Arrays.asList("React", "Node.js", "AWS", "MongoDB", "TypeScript"));
        job1.setRequirements(Arrays.asList("5+ years experience", "B.Tech/M.Tech in CS"));
        job1.setPostedBy(techCorpRecruiterId);
        job1.setRecruiterId(techCorpRecruiterId);
        job1.setPostedDate(LocalDateTime.now().minusDays(2));
        job1.setExpiryDate(LocalDateTime.now().plusDays(30));
        job1.setApplicationsCount(120);
        job1.setViewsCount(450);

        Job job2 = new Job();
        job2.setTitle("ML Engineer");
        job2.setCompany("AI Innovations");
        job2.setLocation("Remote");
        job2.setType("Full-time");
        job2.setSalaryRange("₹60-80 LPA");
        job2.setDescription("Build cutting-edge ML models for recommendation systems and natural language processing.");
        job2.setSkills(Arrays.asList("Python", "TensorFlow", "PyTorch", "NLP", "AWS"));
        job2.setRequirements(Arrays.asList("3+ years ML experience", "PhD/Masters preferred"));
        job2.setPostedBy(aiInnovationsRecruiterId);
        job2.setRecruiterId(aiInnovationsRecruiterId);
        job2.setPostedDate(LocalDateTime.now().minusDays(5));
        job2.setExpiryDate(LocalDateTime.now().plusDays(45));
        job2.setApplicationsCount(85);
        job2.setViewsCount(320);

        Job job3 = new Job();
        job3.setTitle("DevOps Architect");
        job3.setCompany("TechCorp Inc");
        job3.setLocation("Hyderabad, India");
        job3.setType("Full-time");
        job3.setSalaryRange("₹70-90 LPA");
        job3.setDescription("Design and implement scalable cloud infrastructure for high-traffic applications.");
        job3.setSkills(Arrays.asList("Kubernetes", "Docker", "AWS", "CI/CD", "Terraform"));
        job3.setRequirements(Arrays.asList("7+ years DevOps experience", "Cloud certifications preferred"));
        job3.setPostedBy(techCorpRecruiterId);
        job3.setRecruiterId(techCorpRecruiterId);
        job3.setPostedDate(LocalDateTime.now().minusDays(1));
        job3.setExpiryDate(LocalDateTime.now().plusDays(60));
        job3.setApplicationsCount(95);
        job3.setViewsCount(380);

        Job job4 = new Job();
        job4.setTitle("Frontend Developer");
        job4.setCompany("DesignHub");
        job4.setLocation("Mumbai, India");
        job4.setType("Full-time");
        job4.setSalaryRange("₹30-50 LPA");
        job4.setDescription("Create beautiful, responsive user interfaces with modern web technologies.");
        job4.setSkills(Arrays.asList("React", "TypeScript", "CSS", "HTML5", "Tailwind"));
        job4.setRequirements(Arrays.asList("3+ years frontend experience", "Portfolio required"));
        job4.setPostedBy("system-generated-id");
        job4.setRecruiterId("system-generated-id");
        job4.setPostedDate(LocalDateTime.now().minusDays(10));
        job4.setExpiryDate(LocalDateTime.now().plusDays(25));
        job4.setApplicationsCount(150);
        job4.setViewsCount(520);

        Job job5 = new Job();
        job5.setTitle("Data Scientist");
        job5.setCompany("AI Innovations");
        job5.setLocation("Pune, India");
        job5.setType("Full-time");
        job5.setSalaryRange("₹40-60 LPA");
        job5.setDescription("Analyze complex datasets and build predictive models for business insights.");
        job5.setSkills(Arrays.asList("Python", "R", "SQL", "Machine Learning", "Statistics"));
        job5.setRequirements(Arrays.asList("4+ years data science experience", "Masters in Statistics/CS"));
        job5.setPostedBy(aiInnovationsRecruiterId);
        job5.setRecruiterId(aiInnovationsRecruiterId);
        job5.setPostedDate(LocalDateTime.now());
        job5.setExpiryDate(LocalDateTime.now().plusDays(40));
        job5.setApplicationsCount(110);
        job5.setViewsCount(410);

        Job job6 = new Job();
        job6.setTitle("Backend Engineer");
        job6.setCompany("TechCorp Inc");
        job6.setLocation("Chennai, India");
        job6.setType("Full-time");
        job6.setSalaryRange("₹35-55 LPA");
        job6.setDescription("Build robust APIs and microservices for enterprise applications.");
        job6.setSkills(Arrays.asList("Java", "Spring Boot", "PostgreSQL", "Redis", "Kafka"));
        job6.setRequirements(Arrays.asList("4+ years backend experience", "Microservices expertise"));
        job6.setPostedBy(techCorpRecruiterId);
        job6.setRecruiterId(techCorpRecruiterId);
        job6.setPostedDate(LocalDateTime.now().minusDays(3));
        job6.setExpiryDate(LocalDateTime.now().plusDays(35));
        job6.setApplicationsCount(130);
        job6.setViewsCount(470);

        jobRepository.saveAll(Arrays.asList(job1, job2, job3, job4, job5, job6));

        System.out.println("✅ Successfully seeded " + jobRepository.count() + " jobs into MongoDB!");
    }
}
