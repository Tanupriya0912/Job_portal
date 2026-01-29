package com.jobportal.applicationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "applications")
@CompoundIndexes({
    @CompoundIndex(name = "job_applicant_idx", def = "{'jobId': 1, 'applicantId': 1}", unique = true)
})
public class Application {

    @Id
    private String id;

    @Indexed
    private String jobId;

    @Indexed
    private String applicantId;

    @Indexed
    private String recruiterId;

    private String status; // PENDING, ACCEPTED, REJECTED

    private String resumePath;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Application(String jobId, String applicantId, String recruiterId, 
                       String resumePath) {
        this.jobId = jobId;
        this.applicantId = applicantId;
        this.recruiterId = recruiterId;
        this.status = "PENDING";
        this.resumePath = resumePath;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
