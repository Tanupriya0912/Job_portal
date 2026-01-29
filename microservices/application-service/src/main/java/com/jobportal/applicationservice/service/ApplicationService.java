package com.jobportal.applicationservice.service;

import com.jobportal.applicationservice.dto.ApplicationCountDTO;
import com.jobportal.applicationservice.dto.ApplicationResponse;
import com.jobportal.applicationservice.model.Application;
import com.jobportal.applicationservice.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final FileUploadService fileUploadService;
    private final RestTemplate restTemplate;

    @Value("${service.urls.job:http://localhost:3003}")
    private String jobServiceUrl;

    public ApplicationResponse applyForJob(String userId, String jobId, MultipartFile resume) throws Exception {
        // Validate job exists and get recruiter info
        String recruiterId = null;
        try {
            String jobUrl = jobServiceUrl + "/api/v1/jobs/internal/" + jobId;
            // The internal endpoint returns JobResponse directly, not wrapped
            Map<String, Object> jobResponse = restTemplate.getForObject(jobUrl, Map.class);
            log.info("Job response: {}", jobResponse);
            
            // Extract recruiter ID from job details
            if (jobResponse != null) {
                recruiterId = (String) jobResponse.get("createdBy");
                log.info("Extracted recruiter ID: {}", recruiterId);
            }
        } catch (Exception e) {
            log.error("Error fetching job: {}", e.getMessage());
            throw new IllegalArgumentException("Job not found");
        }
        
        if (recruiterId == null) {
            log.error("Recruiter ID is null for job: {}", jobId);
            throw new IllegalArgumentException("Could not determine recruiter for this job");
        }

        // Check if already applied
        if (applicationRepository.findByJobIdAndApplicantId(jobId, userId).isPresent()) {
            throw new IllegalArgumentException("You have already applied for this job");
        }

        // Upload resume
        String resumePath = fileUploadService.uploadResume(resume);

        Application application = new Application(jobId, userId, recruiterId, resumePath);
        Application savedApp = applicationRepository.save(application);
        
        log.info("Application created: {} for job: {} by user: {} with recruiter: {}", savedApp.getId(), jobId, userId, recruiterId);
        return mapToResponse(savedApp);
    }

    public List<ApplicationResponse> getApplicantApplications(String userId) {
        return applicationRepository.findByApplicantId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<ApplicationResponse> getRecruiterApplications(String recruiterId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return applicationRepository.findByRecruiterId(recruiterId, pageable)
                .map(this::mapToResponse);
    }

    public List<ApplicationResponse> getRecruiterApplicationsList(String recruiterId) {
        return applicationRepository.findByRecruiterId(recruiterId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<?> getAvailableJobsForApplicant(String userId) {
        try {
            // Call job-service directly (internal service communication)
            String jobsUrl = jobServiceUrl + "/api/v1/jobs?page=0&size=1000";
            Map<String, Object> response = restTemplate.getForObject(jobsUrl, Map.class);
            
            if (response != null && response.containsKey("result")) {
                Object result = response.get("result");
                if (result instanceof Map) {
                    Map<String, Object> resultMap = (Map<String, Object>) result;
                    Object content = resultMap.get("content");
                    if (content instanceof List) {
                        return (List<?>) content;
                    }
                }
            }
            return List.of();
        } catch (Exception e) {
            log.error("Error fetching available jobs for applicant: {}", e.getMessage());
            return List.of();
        }
    }

    public ApplicationResponse updateApplicationStatus(String applicationId, String userId, String status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!application.getRecruiterId().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own applications");
        }

        application.setStatus(status.toUpperCase());
        application.setUpdatedAt(LocalDateTime.now());
        Application updatedApp = applicationRepository.save(application);
        
        log.info("Application status updated: {} to {}", applicationId, status);
        return mapToResponse(updatedApp);
    }

    public ApplicationCountDTO getApplicationCounts() {
        long pending = applicationRepository.countByStatus("PENDING");
        long accepted = applicationRepository.countByStatus("ACCEPTED");
        long rejected = applicationRepository.countByStatus("REJECTED");

        return new ApplicationCountDTO(pending, accepted, rejected);
    }

    private ApplicationResponse mapToResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setJobId(application.getJobId());
        response.setApplicantId(application.getApplicantId());
        response.setRecruiterId(application.getRecruiterId());
        response.setStatus(application.getStatus());
        response.setResumePath(application.getResumePath());
        response.setCreatedAt(application.getCreatedAt() != null ? application.getCreatedAt().toString() : null);
        response.setUpdatedAt(application.getUpdatedAt() != null ? application.getUpdatedAt().toString() : null);
        
        // Fetch and include job details
        try {
            Map<String, Object> jobDetails = getJobDetails(application.getJobId());
            if (jobDetails != null) {
                response.setJobTitle((String) jobDetails.get("title"));
                response.setJobPosition((String) jobDetails.get("position"));
                response.setJobCompany((String) jobDetails.get("company"));
            }
        } catch (Exception e) {
            log.warn("Could not fetch job details for jobId: {}", application.getJobId());
        }
        
        return response;
    }
    
    private Map<String, Object> getJobDetails(String jobId) {
        try {
            String jobUrl = jobServiceUrl + "/api/v1/jobs/internal/" + jobId;
            Map<String, Object> jobResponse = restTemplate.getForObject(jobUrl, Map.class);
            if (jobResponse != null) {
                return jobResponse;
            }
        } catch (Exception e) {
            log.error("Error fetching job details for jobId {}: {}", jobId, e.getMessage());
        }
        return null;
    }

    public byte[] getResumeBytes(String applicationId) throws Exception {
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        String resumePath = application.getResumePath();
        if (resumePath == null || resumePath.isBlank()) {
            throw new IllegalArgumentException("No resume available for this application");
        }

        try {
            return fileUploadService.readFile(resumePath);
        } catch (Exception e) {
            log.error("Error reading resume file for application {}: {}", applicationId, e.getMessage());
            throw new IllegalArgumentException("Could not read resume file");
        }
    }

}
