package com.jobportal.jobservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private String id;
    private String title;
    private String description;
    private String salary;
    private String location;
    private String company;
    private String position;
    private String jobType;
    private String status;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
}
