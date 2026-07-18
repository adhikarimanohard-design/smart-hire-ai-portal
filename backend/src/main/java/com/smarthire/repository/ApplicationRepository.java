package com.smarthire.repository;

import com.smarthire.model.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplicationRepository
        extends MongoRepository<Application, String> {

    List<Application> findByUserId(String userId);
    List<Application> findByJobId(String jobId);
    List<Application> findByStatus(String status);
    boolean existsByUserIdAndJobId(String userId, String jobId);
    List<Application> findByJobIdOrderByMatchScoreDesc(String jobId);
    List<Application> findByJobIdAndStatus(String jobId, String status);
    List<Application> findByAppliedAtAfter(LocalDateTime date);
    List<Application> findByJobIdIn(List<String> jobIds);
    long countByJobId(String jobId);
    long countByJobIdAndStatus(String jobId, String status);
    void deleteByJobId(String jobId);
}
