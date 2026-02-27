package com.impacthub.backend.dto.response;

import com.impacthub.backend.entity.ApprovalStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminNgoDetailResponse {
    private Long id;
    private String orgName;
    private String fullName;
    private String name;
    private String email;
    private String userEmail;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String registrationNumber;
    private Integer yearFounded;
    private String ngoType;
    private List<String> causeFocus;
    private String mission;
    private String vision;
    private String websiteUrl;
    private String panNumber;
    private String tanNumber;
    private String gstNumber;
    private Boolean is12aRegistered;
    private Boolean is80gRegistered;
    private Boolean fcraRegistered;
    private String logoUrl;
    private String coverImageUrl;
    private String primaryContactName;
    private String primaryContactDesignation;
    private String primaryContactPhone;
    private String primaryContactEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ApprovalStatus approvalStatus;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private LocalDateTime rejectedAt;
    private String rejectedBy;
    private String rejectedReason;
    private LocalDateTime suspendedAt;
    private String suspendedBy;
    private String suspendedReason;
    private LocalDateTime reviewedAt;
    private String reviewedBy;
}
