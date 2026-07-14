package com.smarthire.repository;

import com.smarthire.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository; // <-- ADD THIS IMPORT
import java.util.Optional;

@Repository // <-- ADD THIS ANNOTATION
public interface UserRepository extends MongoRepository<User, String> { 
    
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
