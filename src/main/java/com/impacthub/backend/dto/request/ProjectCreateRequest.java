package com.impacthub.backend.dto.request;

import com.impacthub.backend.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ProjectCreateRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String objectives;
    private String cause;
    private String location;
    private Project.ProjectStatus status;

    @NotNull(message = "Funding goal is required")
    private BigDecimal fundingGoal;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer beneficiaries;
    private String imageUrl;
    private List<String> requiredResources;
    private Integer volunteersNeeded;
}
