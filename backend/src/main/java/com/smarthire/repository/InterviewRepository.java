package com.smarthire.repository;

import com.smarthire.model.Interview;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository
        extends MongoRepository<Interview, String> {

    List<Interview> findByRecruiterIdOrderByScheduledAtAsc(String recruiterId);
    List<Interview> findByCandidateId(String candidateId);
    List<Interview> findByApplicationId(String applicationId);
    List<Interview> findByRecruiterIdAndStatus(String recruiterId, String status);
    long countByRecruiterIdAndStatus(String recruiterId, String status);
}
