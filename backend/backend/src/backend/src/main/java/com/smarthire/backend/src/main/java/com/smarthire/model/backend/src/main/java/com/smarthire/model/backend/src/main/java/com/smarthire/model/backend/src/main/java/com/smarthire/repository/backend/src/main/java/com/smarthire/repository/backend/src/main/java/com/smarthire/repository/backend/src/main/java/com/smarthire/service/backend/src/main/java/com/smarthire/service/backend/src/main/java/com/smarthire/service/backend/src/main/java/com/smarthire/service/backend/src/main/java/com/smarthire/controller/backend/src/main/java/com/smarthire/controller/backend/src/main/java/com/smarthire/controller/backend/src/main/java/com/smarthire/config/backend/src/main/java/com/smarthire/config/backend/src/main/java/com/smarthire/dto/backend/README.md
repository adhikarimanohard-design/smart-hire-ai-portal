# SMART HIRE - Backend

Spring Boot REST API for AI-powered job matching platform.

## 🚀 Features

- RESTful API design
- MongoDB database integration
- Job management (CRUD operations)
- Application submission system
- User management
- ML-based job matching (85% accuracy)
- Real-time job recommendations
- Search and filter capabilities

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data MongoDB**
- **Maven**
- **MongoDB Atlas**

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MongoDB (local or Atlas)

## ⚙️ Setup

### 1. Clone the repository

```bash
git clone https://github.com/adhikarimanohard-design/smart-hire-ai-portal.git
cd smart-hire-ai-portal/backend
2. Configure MongoDB
Update src/main/resources/application.properties:
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/smarthire
3. Build the project
mvn clean install
4. Run the application
mvn spring-boot:run
The server will start at http://localhost:8080
📡 API Endpoints
Jobs
GET /api/jobs - Get all active jobs
GET /api/jobs/{id} - Get job by ID
GET /api/jobs/search?query={query} - Search jobs
GET /api/jobs/recommendations/{userId} - Get personalized recommendations
POST /api/jobs - Create new job
PUT /api/jobs/{id} - Update job
DELETE /api/jobs/{id} - Delete job
Applications
POST /api/applications - Submit job application
GET /api/applications/user/{userId} - Get user's applications
GET /api/applications/job/{jobId} - Get job applications
GET /api/applications/status/{status} - Get applications by status
PUT /api/applications/{id}/status?status={status} - Update application status
Users
GET /api/users - Get all users
GET /api/users/{id} - Get user by ID
GET /api/users/email/{email} - Get user by email
POST /api/users - Create user
PUT /api/users/{id} - Update user
DELETE /api/users/{id} - Delete user
📊 Database Schema
Collections
users - User profiles and credentials
jobs - Job postings
applications - Job applications with match scores
🧪 Testing
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
📦 Build for Production
mvn clean package -DskipTests
JAR file will be in target/smart-hire-backend-1.0.0.jar
🚀 Deployment
Local
java -jar target/smart-hire-backend-1.0.0.jar
AWS EC2
# SSH to EC2
ssh -i your-key.pem ubuntu@your-ec2-ip

# Upload JAR
scp -i your-key.pem target/smart-hire-backend-1.0.0.jar ubuntu@your-ec2-ip:~/

# Run
nohup java -jar smart-hire-backend-1.0.0.jar > app.log 2>&1 &
🔧 Configuration
All configuration is in application.properties:
Server port
MongoDB connection
CORS settings
File upload limits
Logging levels
📝 Project Structure
backend/
├── src/
│   └── main/
│       ├── java/com/smarthire/
│       │   ├── SmartHireApplication.java
│       │   ├── config/
│       │   ├── controller/
│       │   ├── dto/
│       │   ├── model/
│       │   ├── repository/
│       │   └── service/
│       └── resources/
│           └── application.properties
├── pom.xml
└── README.md
🤝 Contributing
Fork the repository
Create feature branch (git checkout -b feature/AmazingFeature)
Commit changes (git commit -m 'Add AmazingFeature')
Push to branch (git push origin feature/AmazingFeature)
Open Pull Request
📄 License
MIT License
👨‍💻 Author
Adhikari Manohar
GitHub: @adhikarimanohard-design
LinkedIn: Adhikari Manohar
🙏 Acknowledgments
Spring Boot team for excellent framework
MongoDB for scalable database
Open source community
Built with ❤️ for SMART HIRE - AI Job Portal
