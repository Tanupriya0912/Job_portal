package com.jobportal.jobservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    private String title;

    private String description;

    private String salary;

    private String location;

    private String company;

    private String position;

    private String jobType; // FULL_TIME, PART_TIME, CONTRACT

    private String status; // ACTIVE, CLOSED

    @Indexed
    private String createdBy; // userId of recruiter

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Job(String title, String description, String salary, String location, 
               String jobType, String status, String createdBy) {
        this.title = title;
        this.description = description;
        this.salary = salary;
        this.location = location;
        this.jobType = jobType;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Job(String title, String description, String salary, String location, String company, String position,
               String jobType, String status, String createdBy) {
        this.title = title;
        this.description = description;
        this.salary = salary;
        this.location = location;
        this.company = company;
        this.position = position;
        this.jobType = jobType;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
