package com.impacthub.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NgoLogoUploadResponse {
    private String logoUrl;
    private String publicId;
}
