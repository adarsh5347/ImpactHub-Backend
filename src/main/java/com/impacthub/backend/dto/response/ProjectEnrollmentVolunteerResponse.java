package com.impacthub.backend.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEnrollmentVolunteerResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String city;
    private LocalDateTime enrolledAt;
}
