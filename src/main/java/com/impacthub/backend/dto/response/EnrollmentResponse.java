package com.impacthub.backend.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    private String message;
    private Long enrollmentId;
    private Long projectId;
    private Long volunteerId;
    private String status;
    private LocalDateTime enrolledAt;
}
