package com.jobportal.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    @JsonProperty("totalUsers")
    private long totalUsers;
    
    @JsonProperty("totalAdmins")
    private long totalAdmins;
    
    @JsonProperty("totalRecruiters")
    private long totalRecruiters;
    
    @JsonProperty("totalApplicants")
    private long totalApplicants;
    
    @JsonProperty("totalJobs")
    private long totalJobs;
    
    @JsonProperty("pendingApplications")
    private long pendingApplications;
    
    @JsonProperty("acceptedApplications")
    private long acceptedApplications;
    
    @JsonProperty("rejectedApplications")
    private long rejectedApplications;
}
