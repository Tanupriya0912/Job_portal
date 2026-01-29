package com.jobportal.applicationservice.controller;

import com.jobportal.applicationservice.dto.ApiResponse;
import com.jobportal.applicationservice.dto.ApplicationCountDTO;
import com.jobportal.applicationservice.dto.ApplicationResponse;
import com.jobportal.applicationservice.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyForJob(
            @RequestHeader("X-USER-ID") String userId,
            @RequestParam String jobId,
            @RequestParam(required = false) MultipartFile resume) {
        try {
            ApplicationResponse response = applicationService.applyForJob(userId, jobId, resume);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, response, "Application submitted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            log.error("Apply for job error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to submit application"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications(
            @RequestHeader("X-USER-ID") String userId) {
        try {
            List<ApplicationResponse> applications = applicationService.getApplicantApplications(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, applications, "Applications retrieved successfully"));
        } catch (Exception e) {
            log.error("Get applications error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve applications"));
        }
    }

    @GetMapping("/recruiter")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getRecruiterApplications(
            @RequestHeader("X-USER-ID") String recruiterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ApplicationResponse> applications = applicationService.getRecruiterApplications(recruiterId, page, size);
            return ResponseEntity.ok(new ApiResponse<>(true, applications, "Applications retrieved successfully"));
        } catch (Exception e) {
            log.error("Get recruiter applications error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve applications"));
        }
    }

    @GetMapping("/recruiter-applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getRecruiterApplicationsList(
            @RequestHeader("X-USER-ID") String recruiterId) {
        try {
            List<ApplicationResponse> applications = applicationService.getRecruiterApplicationsList(recruiterId);
            return ResponseEntity.ok(new ApiResponse<>(true, applications, "Applications retrieved successfully"));
        } catch (Exception e) {
            log.error("Get recruiter applications list error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve applications"));
        }
    }

    @GetMapping("/applicant-jobs")
    public ResponseEntity<ApiResponse<List<?>>> getApplicantJobs(
            @RequestHeader("X-USER-ID") String userId) {
        try {
            List<?> jobs = applicationService.getAvailableJobsForApplicant(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, jobs, "Jobs retrieved successfully"));
        } catch (Exception e) {
            log.error("Get applicant jobs error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve jobs"));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable String id,
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            ApplicationResponse response = applicationService.updateApplicationStatus(id, userId, status);
            return ResponseEntity.ok(new ApiResponse<>(true, response, "Application status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            log.error("Update application status error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to update application"));
        }
    }

    @GetMapping("/{id}/download-resume")
    public ResponseEntity<?> downloadResume(@PathVariable String id) {
        try {
            byte[] data = applicationService.getResumeBytes(id);
            org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(data);

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resume")
                    .contentLength(data.length)
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            log.error("Download resume error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, null, "Could not download resume"));
        }
    }

    @GetMapping("/internal/counts")
    public ResponseEntity<ApplicationCountDTO> getApplicationCounts() {
        try {
            ApplicationCountDTO counts = applicationService.getApplicationCounts();
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            log.error("Error getting application counts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
