package com.impacthub.backend.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.impacthub.backend.config.FlexibleLocalDateDeserializer;
import com.impacthub.backend.entity.Project;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ProjectUpdateRequest {
    private String title;
    private String description;
    private String objectives;
    private String cause;
    private String location;
    private Project.ProjectStatus status;
    private BigDecimal fundingGoal;
    private BigDecimal fundsRaised;
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate startDate;
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate endDate;
    private Integer beneficiaries;
    private String imageUrl;
    private List<String> requiredResources;
    private Integer volunteersNeeded;
    private Integer volunteersEnrolled;
}
