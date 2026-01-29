package com.jobportal.userservice.controller;

import com.jobportal.userservice.dto.ApiResponse;
import com.jobportal.userservice.dto.AdminStatsResponse;
import com.jobportal.userservice.dto.MonthlyStatsDTO;
import com.jobportal.userservice.dto.UpdateProfileRequest;
import com.jobportal.userservice.dto.UpdateRoleRequest;
import com.jobportal.userservice.dto.UserResponse;
import com.jobportal.userservice.service.AdminService;
import com.jobportal.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AdminService adminService;

    @PatchMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestHeader("X-USER-ID") String userId,
            @ModelAttribute UpdateProfileRequest request) {
        try {
            UserResponse response = userService.updateProfile(userId, request);
            return ResponseEntity.ok(new ApiResponse<>(true, response, "Profile updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            log.error("Update profile error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to update profile"));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestHeader(value = "X-USER-ROLE", required = false) String userRole) {
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, null, "Only admins can access this resource"));
        }

        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse<>(true, users, "Users retrieved successfully"));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable String id,
            @RequestHeader(value = "X-USER-ROLE", required = false) String userRole,
            @RequestBody UpdateRoleRequest request) {
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, null, "Only admins can update user roles"));
        }

        try {
            UserResponse response = userService.updateUserRole(id, request.getRole());
            return ResponseEntity.ok(new ApiResponse<>(true, response, "Role updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            log.error("Update role error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to update role"));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable String id,
            @RequestHeader(value = "X-USER-ROLE", required = false) String userRole) {
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, null, "Only admins can delete users"));
        }

        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse<>(true, null, "User deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            log.error("Delete user error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to delete user"));
        }
    }

    @GetMapping("/admin/info")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getAdminStats(
            @RequestHeader(value = "X-USER-ROLE", required = false) String userRole) {
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, null, "Only admins can access this resource"));
        }

        AdminStatsResponse stats = adminService.getSystemStats();
        return ResponseEntity.ok(new ApiResponse<>(true, stats, "Admin stats retrieved"));
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponse<List<MonthlyStatsDTO>>> getMonthlyStats(
            @RequestHeader(value = "X-USER-ROLE", required = false) String userRole) {
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, null, "Only admins can access this resource"));
        }

        List<MonthlyStatsDTO> stats = adminService.getMonthlyStats();
        return ResponseEntity.ok(new ApiResponse<>(true, stats, "Monthly stats retrieved"));
    }

    @GetMapping("/internal/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
