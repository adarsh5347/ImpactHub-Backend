package com.impacthub.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ngo_id", nullable = false)
    private NGO ngo;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String objectives;

    private String cause;
    private String location;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.ONGOING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "funding_goal", precision = 15, scale = 2)
    private BigDecimal fundingGoal;

    @Column(name = "funds_raised", precision = 15, scale = 2)
    private BigDecimal fundsRaised = BigDecimal.ZERO;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private Integer beneficiaries = 0;

    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_required_resources", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "resource")
    private List<String> requiredResources;

    @Column(name = "volunteers_needed")
    private Integer volunteersNeeded = 0;

    @Column(name = "volunteers_enrolled")
    private Integer volunteersEnrolled = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ProjectStatus {
        DRAFT, OPEN, ONGOING, COMPLETED, FUNDED
    }
}
