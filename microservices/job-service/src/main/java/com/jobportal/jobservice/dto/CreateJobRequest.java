package com.jobportal.jobservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobRequest {
    private String title;
    private String description;
    private String salary;
    private String location;
    private String company;
    private String position;
    private String jobType; // FULL_TIME, PART_TIME, CONTRACT
    private String status; // ACTIVE, CLOSED
}
