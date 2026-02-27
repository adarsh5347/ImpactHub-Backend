package com.impacthub.backend.dto.response;

import com.impacthub.backend.entity.Project;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private Long ngoId;
    private String ngoName;
    private String title;
    private String description;
    private String objectives;
    private String cause;
    private String location;
    private Project.ProjectStatus status;
    private BigDecimal fundingGoal;
    private BigDecimal fundsRaised;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer beneficiaries;
    private String imageUrl;
    private List<String> requiredResources;
    private Integer volunteersNeeded;
    private Integer volunteersEnrolled;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
