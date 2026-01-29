package com.jobportal.jobservice.repository;

import com.jobportal.jobservice.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {

    List<Job> findByCreatedBy(String userId);

    Page<Job> findAll(Pageable pageable);

    Page<Job> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);

    @Query("{ $text: { $search: ?0 } }")
    Page<Job> search(String searchTerm, Pageable pageable);

    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    Page<Job> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("{ 'location': { $regex: ?0, $options: 'i' } }")
    Page<Job> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } }, { 'location': { $regex: ?0, $options: 'i' } } ] }")
    Page<Job> searchByMultipleFields(String searchTerm, Pageable pageable);

    @Query("{ $and: [ { $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } }, { 'location': { $regex: ?0, $options: 'i' } } ] }, { 'status': ?1 } ] }")
    Page<Job> searchByMultipleFieldsAndStatus(String searchTerm, String status, Pageable pageable);
}
