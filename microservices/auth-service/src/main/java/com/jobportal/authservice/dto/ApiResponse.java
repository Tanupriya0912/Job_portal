package com.jobportal.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    @JsonProperty("status")
    private boolean status;
    
    @JsonProperty("result")
    private T result;
    
    @JsonProperty("message")
    private String message;
}
