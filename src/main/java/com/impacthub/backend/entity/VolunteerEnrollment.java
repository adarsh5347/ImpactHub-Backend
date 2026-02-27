package com.impacthub.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "volunteer_enrollments",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"volunteer_id", "project_id"},
                name = "unique_enrollment"
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @CreationTimestamp
    @Column(name = "enrollment_date", updatable = false)
    private LocalDateTime enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "hours_contributed")
    private Integer hoursContributed = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum EnrollmentStatus {
        ACTIVE,
        COMPLETED,
        CANCELLED
    }
}