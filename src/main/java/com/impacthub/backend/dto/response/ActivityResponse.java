package com.impacthub.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {
    private Long enrollmentId;
    private Long projectId;
    private String projectTitle;
    private String status;
    private Integer hoursContributed;
    private LocalDateTime enrollmentDate;
}
