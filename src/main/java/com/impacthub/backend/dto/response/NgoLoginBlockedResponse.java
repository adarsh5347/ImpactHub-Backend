package com.impacthub.backend.dto.response;

import com.impacthub.backend.entity.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NgoLoginBlockedResponse {
    private String code;
    private String message;
    private ApprovalStatus ngoStatus;
    private String rejectionReason;
    private String suspensionReason;
}
