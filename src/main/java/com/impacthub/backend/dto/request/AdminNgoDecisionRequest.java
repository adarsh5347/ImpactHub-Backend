package com.impacthub.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminNgoDecisionRequest {
    @NotBlank(message = "Reason is required")
    private String reason;
}
