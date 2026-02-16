package com.smarthire;

import com.smarthire.model.Job;
import com.smarthire.model.User;
import com.smarthire.repository.JobRepository;
import com.smarthire.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

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
        loadJobs();
        loadUsers();
    }

    private void loadJobs() {
        // Only seed if database is empty
        if (jobRepository.count() > 0) {
            System.out.println("📊 Database already has " + jobRepository.count() + " jobs. Skipping seeding.");
            return;
        }
        
        System.out.println("🌱 Seeding database with jobs...");

        // Job 1: Senior Full Stack Engineer
        Job job1 = new Job();
        job1.setTitle("Senior Full Stack Engineer");
        job1.setCompany("TechCorp Inc");
        job1.setLocation("Bangalore, India");
        job1.setType("Full-time");
        job1.setSalaryRange("₹50-70 LPA");
        job1.setDescription("Leading tech company seeking experienced full stack engineer to build scalable cloud applications.");
        job1.setSkills(Arrays.asList("React", "Node.js", "AWS", "MongoDB", "TypeScript"));
        job1.setRequirements(Arrays.asList("5+ years experience", "B.Tech/M.Tech in CS"));
        job1.setPostedBy("HR Team");
        job1.setExpiryDate(LocalDateTime.now().plusDays(30));
        job1.setApplicationsCount(120);
        job1.setViewsCount(450);

        // Job 2: ML Engineer
        Job job2 = new Job();
        job2.setTitle("ML Engineer");
        job2.setCompany("AI Innovations");
        job2.setLocation("Remote");
        job2.setType("Full-time");
        job2.setSalaryRange("₹60-80 LPA");
        job2.setDescription("Build cutting-edge ML models for recommendation systems and natural language processing.");
        job2.setSkills(Arrays.asList("Python", "TensorFlow", "PyTorch", "NLP", "AWS"));
        job2.setRequirements(Arrays.asList("3+ years ML experience", "PhD/Masters preferred"));
        job2.setPostedBy("AI Team");
        job2.setExpiryDate(LocalDateTime.now().plusDays(45));
        job2.setApplicationsCount(85);
        job2.setViewsCount(320);

        // Job 3: DevOps Architect
        Job job3 = new Job();
        job3.setTitle("DevOps Architect");
        job3.setCompany("CloudScale Systems");
        job3.setLocation("Hyderabad, India");
        job3.setType("Full-time");
        job3.setSalaryRange("₹70-90 LPA");
        job3.setDescription("Design and implement scalable cloud infrastructure for high-traffic applications.");
        job3.setSkills(Arrays.asList("Kubernetes", "Docker", "AWS", "CI/CD", "Terraform"));
        job3.setRequirements(Arrays.asList("7+ years DevOps experience", "Cloud certifications preferred"));
        job3.setPostedBy("Infrastructure Team");
        job3.setExpiryDate(LocalDateTime.now().plusDays(60));
        job3.setApplicationsCount(95);
        job3.setViewsCount(380);

        // Job 4: Frontend Developer
        Job job4 = new Job();
        job4.setTitle("Frontend Developer");
        job4.setCompany("DesignHub");
        job4.setLocation("Mumbai, India");
        job4.setType("Full-time");
        job4.setSalaryRange("₹30-50 LPA");
        job4.setDescription("Create beautiful, responsive user interfaces with modern web technologies.");
        job4.setSkills(Arrays.asList("React", "TypeScript", "CSS", "HTML5", "Tailwind"));
        job4.setRequirements(Arrays.asList("3+ years frontend experience", "Portfolio required"));
        job4.setPostedBy("Design Team");
        job4.setExpiryDate(LocalDateTime.now().plusDays(25));
        job4.setApplicationsCount(150);
        job4.setViewsCount(520);

        // Job 5: Data Scientist
        Job job5 = new Job();
        job5.setTitle("Data Scientist");
        job5.setCompany("Analytics Pro");
        job5.setLocation("Pune, India");
        job5.setType("Full-time");
        job5.setSalaryRange("₹40-60 LPA");
        job5.setDescription("Analyze complex datasets and build predictive models for business insights.");
        job5.setSkills(Arrays.asList("Python", "R", "SQL", "Machine Learning", "Statistics"));
        job5.setRequirements(Arrays.asList("4+ years data science experience", "Masters in Statistics/CS"));
        job5.setPostedBy("Analytics Team");
        job5.setExpiryDate(LocalDateTime.now().plusDays(40));
        job5.setApplicationsCount(110);
        job5.setViewsCount(410);

        // Job 6: Backend Engineer
        Job job6 = new Job();
        job6.setTitle("Backend Engineer");
        job6.setCompany("ServerTech");
        job6.setLocation("Chennai, India");
        job6.setType("Full-time");
        job6.setSalaryRange("₹35-55 LPA");
        job6.setDescription("Build robust APIs and microservices for enterprise applications.");
        job6.setSkills(Arrays.asList("Java", "Spring Boot", "PostgreSQL", "Redis", "Kafka"));
        job6.setRequirements(Arrays.asList("4+ years backend experience", "Microservices expertise"));
        job6.setPostedBy("Backend Team");
        job6.setExpiryDate(LocalDateTime.now().plusDays(35));
        job6.setApplicationsCount(130);
        job6.setViewsCount(470);

        // Job 7: Mobile Developer
        Job job7 = new Job();
        job7.setTitle("Mobile Developer");
        job7.setCompany("AppWorks");
        job7.setLocation("Delhi, India");
        job7.setType("Full-time");
        job7.setSalaryRange("₹25-45 LPA");
        job7.setDescription("Develop native mobile applications for iOS and Android platforms.");
        job7.setSkills(Arrays.asList("React Native", "Flutter", "iOS", "Android", "Firebase"));
        job7.setRequirements(Arrays.asList("3+ years mobile development", "Published apps required"));
        job7.setPostedBy("Mobile Team");
        job7.setExpiryDate(LocalDateTime.now().plusDays(50));
        job7.setApplicationsCount(140);
        job7.setViewsCount(490);

        // Job 8: Cloud Architect
        Job job8 = new Job();
        job8.setTitle("Cloud Architect");
        job8.setCompany("CloudFirst Solutions");
        job8.setLocation("Remote");
        job8.setType("Full-time");
        job8.setSalaryRange("₹80-100 LPA");
        job8.setDescription("Design and implement enterprise-grade cloud solutions on AWS/Azure/GCP.");
        job8.setSkills(Arrays.asList("AWS", "Azure", "GCP", "Kubernetes", "Terraform"));
        job8.setRequirements(Arrays.asList("8+ years cloud experience", "Multiple cloud certifications"));
        job8.setPostedBy("Cloud Team");
        job8.setExpiryDate(LocalDateTime.now().plusDays(55));
        job8.setApplicationsCount(75);
        job8.setViewsCount(290);

        // Job 9: Product Manager
        Job job9 = new Job();
        job9.setTitle("Product Manager");
        job9.setCompany("ProductHub");
        job9.setLocation("Bangalore, India");
        job9.setType("Full-time");
        job9.setSalaryRange("₹45-65 LPA");
        job9.setDescription("Lead product strategy and execution for innovative SaaS products.");
        job9.setSkills(Arrays.asList("Product Strategy", "Agile", "User Research", "Analytics", "Roadmapping"));
        job9.setRequirements(Arrays.asList("5+ years product management", "Technical background preferred"));
        job9.setPostedBy("Product Team");
        job9.setExpiryDate(LocalDateTime.now().plusDays(42));
        job9.setApplicationsCount(105);
        job9.setViewsCount(395);

        // Job 10: Security Engineer
        Job job10 = new Job();
        job10.setTitle("Security Engineer");
        job10.setCompany("SecureNet");
        job10.setLocation("Hyderabad, India");
        job10.setType("Full-time");
        job10.setSalaryRange("₹55-75 LPA");
        job10.setDescription("Implement security measures and conduct vulnerability assessments.");
        job10.setSkills(Arrays.asList("Cybersecurity", "Penetration Testing", "SIEM", "Firewalls", "Compliance"));
        job10.setRequirements(Arrays.asList("5+ years security experience", "Security certifications (CISSP, CEH)"));
        job10.setPostedBy("Security Team");
        job10.setExpiryDate(LocalDateTime.now().plusDays(38));
        job10.setApplicationsCount(90);
        job10.setViewsCount(340);

        // Save all jobs to MongoDB
        jobRepository.saveAll(Arrays.asList(
            job1, job2, job3, job4, job5, 
            job6, job7, job8, job9, job10
        ));

        System.out.println("✅ Successfully seeded " + jobRepository.count() + " jobs into MongoDB!");
    }

    private void loadUsers() {
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
            System.out.println("ℹ️ Users already exist in database. Skipping.");
}
}
}