package com.impacthub.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NgoCoverUploadResponse {
    private String coverImageUrl;
    private String publicId;
}
