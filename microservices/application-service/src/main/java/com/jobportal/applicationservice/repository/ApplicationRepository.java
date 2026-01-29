package com.jobportal.applicationservice.repository;

import com.jobportal.applicationservice.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {

    List<Application> findByApplicantId(String applicantId);

    List<Application> findByRecruiterId(String recruiterId);

    Page<Application> findByRecruiterId(String recruiterId, Pageable pageable);

    Optional<Application> findByJobIdAndApplicantId(String jobId, String applicantId);

    long countByStatus(String status);

    @Query("{ 'recruiterId': ?0, 'status': ?1 }")
    List<Application> findByRecruiterIdAndStatus(String recruiterId, String status);
}
