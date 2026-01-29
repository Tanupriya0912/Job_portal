package com.jobportal.userservice.service;

import com.jobportal.userservice.dto.UpdateProfileRequest;
import com.jobportal.userservice.dto.UserResponse;
import com.jobportal.userservice.model.User;
import com.jobportal.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final RestTemplate restTemplate;

    @Value("${service.urls.job:http://localhost:3003}")
    private String jobServiceUrl;

    public UserResponse updateProfile(String userId, UpdateProfileRequest request) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            user.setUsername(request.getUsername());
        }

        if (request.getLocation() != null && !request.getLocation().isEmpty()) {
            user.setLocation(request.getLocation());
        }

        if (request.getGender() != null && !request.getGender().isEmpty()) {
            user.setGender(request.getGender());
        }

        if (request.getResume() != null && !request.getResume().isEmpty()) {
            String resumePath = fileUploadService.uploadResume(request.getResume());
            user.setResume(resumePath);
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", userId);
        return mapToResponse(updatedUser);
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Call job-service to delete all jobs by this user
        try {
            String url = jobServiceUrl + "/internal/jobs/user/" + userId;
            restTemplate.delete(url);
            log.info("Deleted jobs for user: {}", userId);
        } catch (Exception e) {
            log.error("Error deleting jobs for user {}: {}", userId, e.getMessage());
        }

        userRepository.deleteById(userId);
        log.info("User deleted: {}", userId);
    }

    public UserResponse updateUserRole(String userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRole(role.toUpperCase());
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        log.info("User role updated: {} to {}", userId, role);
        return mapToResponse(updatedUser);
    }

    public long getUsersByRole(String role) {
        return userRepository.countByRole(role);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setLocation(user.getLocation());
        response.setGender(user.getGender());
        response.setResume(user.getResume());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        response.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        return response;
    }
}
