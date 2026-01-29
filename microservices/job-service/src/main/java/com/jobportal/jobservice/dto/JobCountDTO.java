package com.jobportal.jobservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobCountDTO {
    private long totalJobs;
    private long activeJobs;
    private long closedJobs;
}
