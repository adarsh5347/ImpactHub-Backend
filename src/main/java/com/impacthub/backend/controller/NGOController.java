package com.impacthub.backend.controller;

import com.impacthub.backend.dto.request.NGOUpdateRequest;
import com.impacthub.backend.dto.request.ProjectUpdateRequest;
import com.impacthub.backend.dto.response.NGOResponse;
import com.impacthub.backend.dto.response.NgoCoverUploadResponse;
import com.impacthub.backend.dto.response.NgoLogoUploadResponse;
import com.impacthub.backend.dto.response.PaginatedResponse;
import com.impacthub.backend.dto.response.ProjectEnrollmentGroupResponse;
import com.impacthub.backend.dto.response.ProjectEnrollmentVolunteerResponse;
import com.impacthub.backend.dto.response.ProjectResponse;
import com.impacthub.backend.entity.Project;
import com.impacthub.backend.service.CloudinaryUploadService;
import com.impacthub.backend.service.EnrollmentService;
import com.impacthub.backend.service.NGOService;
import com.impacthub.backend.service.ProjectService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ngos")
@RequiredArgsConstructor
public class NGOController {

    private final NGOService ngoService;
    private final EnrollmentService enrollmentService;
    private final ProjectService projectService;
    private final CloudinaryUploadService cloudinaryUploadService;

    @PostMapping("/logo/upload")
    public ResponseEntity<NgoLogoUploadResponse> uploadNgoLogo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(cloudinaryUploadService.uploadNgoLogo(file));
    }

    @PostMapping("/cover/upload")
    public ResponseEntity<NgoCoverUploadResponse> uploadNgoCover(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(cloudinaryUploadService.uploadNgoCover(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNGO(@PathVariable Long id, Authentication authentication) {
        String currentEmail = getCurrentEmail(authentication);
        boolean isOwner = ngoService.isNgoOwnedByEmail(id, currentEmail);
        if (!isOwner) {
            return ResponseEntity.status(403).body("You can only view your own NGO profile");
        }

        return ResponseEntity.ok(ngoService.getNgoById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNGO(
            @PathVariable Long id,
            @RequestBody NGOUpdateRequest request,
            Authentication authentication
    ) {
        String currentEmail = getCurrentEmail(authentication);
        boolean isOwner = ngoService.isNgoOwnedByEmail(id, currentEmail);
        if (!isOwner) {
            return ResponseEntity.status(403).body("You can only update your own NGO profile");
        }

        NGOResponse updated = ngoService.updateNgo(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNGO(@PathVariable Long id, Authentication authentication) {
        String currentEmail = getCurrentEmail(authentication);
        boolean isOwner = ngoService.isNgoOwnedByEmail(id, currentEmail);
        if (!isOwner) {
            return ResponseEntity.status(403).body("You can only delete your own NGO account");
        }

        ngoService.deactivateNgo(id);
        return ResponseEntity.ok("NGO account deactivated successfully");
    }

    @GetMapping("/{id}/projects")
    public ResponseEntity<List<Project>> getProjectsByNgo(@PathVariable Long id) {
        return ResponseEntity.ok(ngoService.getProjectsByNgo(id));
    }

    @GetMapping("/{ngoId}/projects/{projectId}")
    public ResponseEntity<ProjectResponse> getNgoProject(
            @PathVariable Long ngoId,
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        String currentEmail = getCurrentEmail(authentication);
        return ResponseEntity.ok(projectService.getProjectForNgo(ngoId, projectId, currentEmail));
    }

    @PutMapping("/{ngoId}/projects/{projectId}")
    public ResponseEntity<ProjectResponse> updateNgoProject(
            @PathVariable Long ngoId,
            @PathVariable Long projectId,
            @RequestBody ProjectUpdateRequest request,
            Authentication authentication
    ) {
        String currentEmail = getCurrentEmail(authentication);
        return ResponseEntity.ok(projectService.updateProjectForNgo(ngoId, projectId, request, currentEmail));
    }

    @GetMapping
    public ResponseEntity<?> listNGOs(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String cause,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit
    ) {
        if (search != null || page != null || limit != null) {
            int safePage = page != null ? page : 1;
            int safeLimit = limit != null ? limit : 10;
            PaginatedResponse<NGOResponse> response = ngoService.listNgosPaginated(
                    city, state, cause, verified, search, safePage, safeLimit
            );
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(ngoService.listNgos(city, state, cause, verified));
    }

    @GetMapping("/{ngoId}/enrollments")
    public ResponseEntity<List<ProjectEnrollmentGroupResponse>> getNgoEnrollments(
            @PathVariable Long ngoId,
            Authentication authentication
    ) {
        String currentEmail = getCurrentEmail(authentication);
        return ResponseEntity.ok(enrollmentService.listEnrollmentsForNgo(ngoId, currentEmail));
    }

    @GetMapping("/{ngoId}/projects/{projectId}/enrollments")
    public ResponseEntity<List<ProjectEnrollmentVolunteerResponse>> getProjectEnrollments(
            @PathVariable Long ngoId,
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        String currentEmail = getCurrentEmail(authentication);
        return ResponseEntity.ok(
                enrollmentService.listEnrolledVolunteersForProject(ngoId, projectId, currentEmail)
        );
    }

    @PatchMapping("/{ngoId}/projects/{projectId}/complete")
    public ResponseEntity<ProjectResponse> markProjectCompleted(
            @PathVariable Long ngoId,
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        String currentEmail = getCurrentEmail(authentication);
        return ResponseEntity.ok(projectService.markProjectCompleted(ngoId, projectId, currentEmail));
    }

    private String getCurrentEmail(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated request");
        }
        return authentication.getName();
    }
}
