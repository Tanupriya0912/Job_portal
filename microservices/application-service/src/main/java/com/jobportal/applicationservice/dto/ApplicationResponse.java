package com.jobportal.applicationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private String id;
    private String jobId;
    private String applicantId;
    private String recruiterId;
    private String status;
    private String resumePath;
    private String createdAt;
    private String updatedAt;
    // Job details for easier frontend display
    private String jobTitle;
    private String jobPosition;
    private String jobCompany;
}
