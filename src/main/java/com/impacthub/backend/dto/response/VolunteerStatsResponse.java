package com.impacthub.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerStatsResponse {
    private Long volunteerId;
    private long totalEnrollments;
    private long activeEnrollments;
    private long completedEnrollments;
    private long cancelledEnrollments;
    private long totalHoursContributed;
}
