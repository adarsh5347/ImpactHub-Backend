package com.impacthub.backend.dto.response;

import com.impacthub.backend.entity.NGO;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NGOResponse {
    private Long id;
    private String userEmail;
    private String ngoName;
    private String registrationNumber;
    private Integer yearFounded;
    private NGO.NGOType ngoType;
    private List<String> causeFocus;
    private String mission;
    private String vision;
    private String websiteUrl;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String panNumber;
    private String tanNumber;
    private String gstNumber;
    private Boolean is12aRegistered;
    private Boolean is80gRegistered;
    private Boolean fcraRegistered;
    private String bankAccountNumber;
    private String bankName;
    private String bankIfsc;
    private String bankBranch;
    private String primaryContactName;
    private String primaryContactDesignation;
    private String primaryContactPhone;
    private String primaryContactEmail;
    private Boolean isVerified;
    private LocalDateTime verificationDate;
    private Integer activeProjects;
    private Integer completedProjects;
    private String logoUrl;
    private String logoPublicId;
    private String coverImageUrl;
    private String coverImagePublicId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
