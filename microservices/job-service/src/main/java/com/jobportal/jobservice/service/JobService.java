package com.jobportal.jobservice.service;

import com.jobportal.jobservice.dto.CreateJobRequest;
import com.jobportal.jobservice.dto.JobCountDTO;
import com.jobportal.jobservice.dto.JobResponse;
import com.jobportal.jobservice.model.Job;
import com.jobportal.jobservice.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    public JobResponse createJob(String userId, CreateJobRequest request) {
        Job job = new Job(
                request.getTitle(),
                request.getDescription(),
                request.getSalary(),
                request.getLocation(),
                request.getCompany(),
                request.getPosition(),
                request.getJobType(),
                "ACTIVE",
                userId
        );
        Job savedJob = jobRepository.save(job);
        log.info("Job created: {} by user: {}", savedJob.getId(), userId);
        return mapToResponse(savedJob);
    }

    public JobResponse getJob(String jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        return mapToResponse(job);
    }

    public Page<JobResponse> searchJobs(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Job> jobs;
        if (search == null || search.isEmpty()) {
            jobs = jobRepository.findByStatus("ACTIVE", pageable);
        } else {
            jobs = jobRepository.searchByMultipleFieldsAndStatus(search, "ACTIVE", pageable);
        }
        
        return jobs.map(this::mapToResponse);
    }

    public List<JobResponse> getJobsByRecruiter(String recruiterId) {
        return jobRepository.findByCreatedBy(recruiterId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public JobResponse updateJob(String jobId, String userId, CreateJobRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (!job.getCreatedBy().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own jobs");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setSalary(request.getSalary());
        job.setLocation(request.getLocation());
        job.setCompany(request.getCompany());
        job.setPosition(request.getPosition());
        job.setJobType(request.getJobType());
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }
        job.setUpdatedAt(LocalDateTime.now());

        Job updatedJob = jobRepository.save(job);
        log.info("Job updated: {} by user: {}", jobId, userId);
        return mapToResponse(updatedJob);
    }

    public void deleteJob(String jobId, String userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (!job.getCreatedBy().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own jobs");
        }

        jobRepository.deleteById(jobId);
        log.info("Job deleted: {} by user: {}", jobId, userId);
    }

    public void deleteJobsByUserId(String userId) {
        List<Job> jobs = jobRepository.findByCreatedBy(userId);
        jobRepository.deleteAll(jobs);
        log.info("Deleted {} jobs for user: {}", jobs.size(), userId);
    }

    public JobCountDTO getStats() {
        long totalJobs = jobRepository.count();
        long activeJobs = jobRepository.countByStatus("ACTIVE");
        long closedJobs = jobRepository.countByStatus("CLOSED");

        JobCountDTO stats = new JobCountDTO();
        stats.setTotalJobs(totalJobs);
        stats.setActiveJobs(activeJobs);
        stats.setClosedJobs(closedJobs);

        return stats;
    }

    private JobResponse mapToResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setCompany(job.getCompany());
        response.setPosition(job.getPosition());
        response.setSalary(job.getSalary());
        response.setLocation(job.getLocation());
        response.setJobType(job.getJobType());
        response.setStatus(job.getStatus());
        response.setCreatedBy(job.getCreatedBy());
        response.setCreatedAt(job.getCreatedAt() != null ? job.getCreatedAt().toString() : null);
        response.setUpdatedAt(job.getUpdatedAt() != null ? job.getUpdatedAt().toString() : null);
        return response;
    }
}
