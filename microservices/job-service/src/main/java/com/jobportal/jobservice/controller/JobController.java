package com.jobportal.jobservice.controller;

import com.jobportal.jobservice.dto.ApiResponse;
import com.jobportal.jobservice.dto.CreateJobRequest;
import com.jobportal.jobservice.dto.JobCountDTO;
import com.jobportal.jobservice.dto.JobResponse;
import com.jobportal.jobservice.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @RequestHeader(value = "X-USER-ROLE", required = false) String userRole,
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody CreateJobRequest request) {
        if (!"RECRUITER".equals(userRole) && !"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, null, "Only recruiters can post jobs"));
        }

        try {
            JobResponse response = jobService.createJob(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, response, "Job posted successfully"));
        } catch (Exception e) {
            log.error("Create job error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to create job"));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobResponse>>> searchJobs(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<JobResponse> jobs = jobService.searchJobs(search, page, size);
            return ResponseEntity.ok(new ApiResponse<>(true, jobs, "Jobs retrieved successfully"));
        } catch (Exception e) {
            log.error("Search jobs error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to search jobs"));
        }
    }

    @GetMapping("/my-jobs")
    public ResponseEntity<ApiResponse<List<JobResponse>>> getMyJobs(
            @RequestHeader("X-USER-ID") String userId) {
        try {
            List<JobResponse> jobs = jobService.getJobsByRecruiter(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, jobs, "Your jobs retrieved successfully"));
        } catch (Exception e) {
            log.error("Get my jobs error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve your jobs"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJob(@PathVariable String id) {
        try {
            JobResponse job = jobService.getJob(id);
            return ResponseEntity.ok(new ApiResponse<>(true, job, "Job retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            log.error("Get job error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to retrieve job"));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable String id,
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody CreateJobRequest request) {
        try {
            JobResponse response = jobService.updateJob(id, userId, request);
            return ResponseEntity.ok(new ApiResponse<>(true, response, "Job updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            log.error("Update job error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to update job"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable String id,
            @RequestHeader("X-USER-ID") String userId) {
        try {
            jobService.deleteJob(id, userId);
            return ResponseEntity.ok(new ApiResponse<>(true, null, "Job deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            log.error("Delete job error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to delete job"));
        }
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<JobResponse> getJobInternal(@PathVariable String id) {
        try {
            return ResponseEntity.ok(jobService.getJob(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/internal/user/{userId}")
    public ResponseEntity<Void> deleteJobsByUser(@PathVariable String userId) {
        try {
            jobService.deleteJobsByUserId(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting jobs for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/internal/stats")
    public ResponseEntity<JobCountDTO> getStats() {
        try {
            JobCountDTO stats = jobService.getStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting job stats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/internal/monthly-stats")
    public ResponseEntity<?> getMonthlyStats() {
        // For now, return empty list. Can be enhanced later
        return ResponseEntity.ok(List.of());
    }
}
