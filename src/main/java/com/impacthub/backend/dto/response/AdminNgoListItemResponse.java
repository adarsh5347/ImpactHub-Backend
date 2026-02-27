package com.impacthub.backend.dto.response;

import com.impacthub.backend.entity.ApprovalStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminNgoListItemResponse {
    private Long id;
    private String orgName;
    private String name;
    private String email;
    private String phone;
    private String city;
    private String state;
    private String logoUrl;
    private LocalDateTime createdAt;
    private ApprovalStatus approvalStatus;
}
