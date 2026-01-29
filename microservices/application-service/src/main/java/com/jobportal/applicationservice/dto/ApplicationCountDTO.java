package com.jobportal.applicationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCountDTO {
    private long pending;
    private long accepted;
    private long rejected;
}
