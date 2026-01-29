package com.jobportal.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    private String username;
    
    @Indexed(unique = true)
    private String email;
    
    private String location;
    private String gender;
    private String resume;
    private String role; // USER, RECRUITER, ADMIN
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
