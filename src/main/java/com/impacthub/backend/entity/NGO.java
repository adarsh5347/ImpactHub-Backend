package com.impacthub.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "ngos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NGO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "ngo_name", nullable = false)
    private String ngoName;

    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber;

    @Column(name = "year_founded")
    private Integer yearFounded;

    @Enumerated(EnumType.STRING)
    @Column(name = "ngo_type")
    private NGOType ngoType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ngo_cause_focus", joinColumns = @JoinColumn(name = "ngo_id"))
    @Column(name = "cause")
    private List<String> causeFocus;

    @Column(columnDefinition = "TEXT")
    private String mission;

    @Column(columnDefinition = "TEXT")
    private String vision;

    @Column(name = "website_url")
    private String websiteUrl;

    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "tan_number")
    private String tanNumber;

    @Column(name = "gst_number")
    private String gstNumber;

    @Column(name = "is_12a_registered")
    private Boolean is12aRegistered = false;

    @Column(name = "is_80g_registered")
    private Boolean is80gRegistered = false;

    @Column(name = "fcra_registered")
    private Boolean fcraRegistered = false;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_ifsc")
    private String bankIfsc;

    @Column(name = "bank_branch")
    private String bankBranch;

    @Column(name = "primary_contact_name")
    private String primaryContactName;

    @Column(name = "primary_contact_designation")
    private String primaryContactDesignation;

    @Column(name = "primary_contact_phone")
    private String primaryContactPhone;

    @Column(name = "primary_contact_email")
    private String primaryContactEmail;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "active_projects")
    private Integer activeProjects = 0;

    @Column(name = "completed_projects")
    private Integer completedProjects = 0;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "logo_public_id")
    private String logoPublicId;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "cover_image_public_id")
    private String coverImagePublicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    private String rejectedReason;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "suspended_by")
    private String suspendedBy;

    @Column(name = "suspended_reason", columnDefinition = "TEXT")
    private String suspendedReason;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum NGOType {
        Trust, Society, Section8Company, Other
    }
}
