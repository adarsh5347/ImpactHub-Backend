package com.impacthub.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long pendingNgos;
    private long approvedNgos;
    private long rejectedNgos;
    private long totalVolunteers;
    private long totalProjects;
}
