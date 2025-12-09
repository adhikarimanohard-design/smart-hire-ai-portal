package main.java.com.smarthire.repository;

import com.smarthire.model.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {
    List<Application> findByUserId(String userId);
    List<Application> findByJobId(String jobId);
    List<Application> findByStatus(String status);
    boolean existsByUserIdAndJobId(String userId, String jobId);
}