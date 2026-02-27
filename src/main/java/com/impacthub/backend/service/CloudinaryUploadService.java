package com.impacthub.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.impacthub.backend.dto.response.NgoCoverUploadResponse;
import com.impacthub.backend.dto.response.NgoLogoUploadResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryUploadService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:impacthub/ngos}")
    private String cloudinaryFolder;

    @Value("${cloudinary.cover-folder:impacthub/ngos/covers}")
    private String cloudinaryCoverFolder;

    @Value("${app.upload.logo.max-size-bytes:5242880}")
    private long maxLogoSizeBytes;

    @Value("${app.upload.cover.max-size-bytes:10485760}")
    private long maxCoverSizeBytes;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    public NgoLogoUploadResponse uploadNgoLogo(MultipartFile file) {
        assertCloudinaryConfigured();
        validateImageFile(file, maxLogoSizeBytes);

        try {
            Map<?, ?> uploadResult = uploadImage(file, cloudinaryFolder);

            String secureUrl = uploadResult.get("secure_url") != null
                    ? String.valueOf(uploadResult.get("secure_url"))
                    : null;
            String publicId = uploadResult.get("public_id") != null
                    ? String.valueOf(uploadResult.get("public_id"))
                    : null;

            if (secureUrl == null || secureUrl.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary did not return a secure URL");
            }

            return new NgoLogoUploadResponse(secureUrl, publicId);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image file", ex);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to upload logo image", ex);
        }
    }

    public NgoCoverUploadResponse uploadNgoCover(MultipartFile file) {
        assertCloudinaryConfigured();
        validateImageFile(file, maxCoverSizeBytes);

        try {
            Map<?, ?> uploadResult = uploadImage(file, cloudinaryCoverFolder);

            String secureUrl = uploadResult.get("secure_url") != null
                    ? String.valueOf(uploadResult.get("secure_url"))
                    : null;
            String publicId = uploadResult.get("public_id") != null
                    ? String.valueOf(uploadResult.get("public_id"))
                    : null;

            if (secureUrl == null || secureUrl.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary did not return a secure URL");
            }

            return new NgoCoverUploadResponse(secureUrl, publicId);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image file", ex);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to upload cover image", ex);
        }
    }

    public void deleteByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        if (!isCloudinaryConfigured()) {
            log.warn("Skipping Cloudinary delete because credentials are missing/blank. publicId={}", publicId);
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (Exception ex) {
            log.warn("Failed to delete Cloudinary asset: {}", publicId, ex);
        }
    }

    private void assertCloudinaryConfigured() {
        if (!isCloudinaryConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Cloudinary is not configured. Set cloudinary.cloud-name, cloudinary.api-key, and cloudinary.api-secret."
            );
        }
    }

    private boolean isCloudinaryConfigured() {
        return cloudName != null && !cloudName.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank();
    }

    private void validateImageFile(MultipartFile file, long maxSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG, PNG, and WEBP images are allowed");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image exceeds maximum allowed size");
        }
    }

    private Map<?, ?> uploadImage(MultipartFile file, String folder) throws IOException {
        return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image"
                )
        );
    }
}
