package com.impacthub.backend.exception;

import com.impacthub.backend.entity.ApprovalStatus;
import lombok.Getter;

@Getter
public class NgoLoginBlockedException extends RuntimeException {
    private final String code;
    private final ApprovalStatus ngoStatus;
    private final String rejectionReason;
    private final String suspensionReason;

    public NgoLoginBlockedException(
            String code,
            String message,
            ApprovalStatus ngoStatus,
            String rejectionReason,
            String suspensionReason
    ) {
        super(message);
        this.code = code;
        this.ngoStatus = ngoStatus;
        this.rejectionReason = rejectionReason;
        this.suspensionReason = suspensionReason;
    }
}
