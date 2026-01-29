package com.jobportal.userservice.service;

import com.jobportal.userservice.dto.AdminStatsResponse;
import com.jobportal.userservice.dto.MonthlyStatsDTO;
import com.jobportal.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${service.urls.job:http://localhost:3003}")
    private String jobServiceUrl;

    @Value("${service.urls.application:http://localhost:3004}")
    private String applicationServiceUrl;

    public AdminStatsResponse getSystemStats() {
        AdminStatsResponse stats = new AdminStatsResponse();

        // User counts from database
        long totalUsers = userRepository.count();
        long totalAdmins = userRepository.countByRole("ADMIN");
        long totalRecruiters = userRepository.countByRole("RECRUITER");
        long totalApplicants = userRepository.countByRole("USER");

        stats.setTotalUsers(totalUsers);
        stats.setTotalAdmins(totalAdmins);
        stats.setTotalRecruiters(totalRecruiters);
        stats.setTotalApplicants(totalApplicants);

        // Job counts from job-service
        long totalJobs = 0;
        try {
            String jobUrl = jobServiceUrl + "/internal/stats";
            Map jobStats = restTemplate.getForObject(jobUrl, Map.class);
            if (jobStats != null && jobStats.containsKey("totalJobs")) {
                totalJobs = ((Number) jobStats.get("totalJobs")).longValue();
            }
            log.info("Retrieved job stats from job-service");
        } catch (Exception e) {
            log.error("Error fetching job stats: {}", e.getMessage());
        }
        stats.setTotalJobs(totalJobs);

        // Application counts from application-service
        long pendingApplications = 0;
        long acceptedApplications = 0;
        long rejectedApplications = 0;
        try {
            String appUrl = applicationServiceUrl + "/internal/counts";
            Map appStats = restTemplate.getForObject(appUrl, Map.class);
            if (appStats != null) {
                if (appStats.containsKey("pending")) {
                    pendingApplications = ((Number) appStats.get("pending")).longValue();
                }
                if (appStats.containsKey("accepted")) {
                    acceptedApplications = ((Number) appStats.get("accepted")).longValue();
                }
                if (appStats.containsKey("rejected")) {
                    rejectedApplications = ((Number) appStats.get("rejected")).longValue();
                }
            }
            log.info("Retrieved application stats from application-service");
        } catch (Exception e) {
            log.error("Error fetching application stats: {}", e.getMessage());
        }
        stats.setPendingApplications(pendingApplications);
        stats.setAcceptedApplications(acceptedApplications);
        stats.setRejectedApplications(rejectedApplications);

        return stats;
    }

    public List<MonthlyStatsDTO> getMonthlyStats() {
        try {
            String url = jobServiceUrl + "/internal/monthly-stats";
            List<MonthlyStatsDTO> stats = restTemplate.getForObject(url, List.class);
            log.info("Retrieved monthly stats from job-service");
            return stats != null ? stats : List.of();
        } catch (Exception e) {
            log.error("Error fetching monthly stats: {}", e.getMessage());
            return List.of();
        }
    }
}
