package com.impacthub.backend.controller;

import com.impacthub.backend.dto.request.ProjectCreateRequest;
import com.impacthub.backend.dto.request.ProjectUpdateRequest;
import com.impacthub.backend.dto.response.EnrollmentResponse;
import com.impacthub.backend.dto.response.ProjectResponse;
import com.impacthub.backend.entity.Project;
import com.impacthub.backend.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            Authentication authentication
    ) {
        String currentEmail = getCurrentEmail(authentication);
        ProjectResponse created = projectService.createProject(request, currentEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> listProjects(
            @RequestParam(required = false) Project.ProjectStatus status,
            @RequestParam(required = false) String cause,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long ngoId
    ) {
        return ResponseEntity.ok(projectService.listProjects(status, cause, location, ngoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectUpdateRequest request,
            Authentication authentication
    ) {
        String currentEmail = getCurrentEmail(authentication);
        return ResponseEntity.ok(projectService.updateProject(id, request, currentEmail));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable Long id, Authentication authentication) {
        String currentEmail = getCurrentEmail(authentication);
        projectService.deleteProject(id, currentEmail);
        return ResponseEntity.ok("Project deleted successfully");
    }

    @PostMapping("/{projectId}/enroll")
    public ResponseEntity<EnrollmentResponse> enrollAsVolunteer(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        String currentEmail = getCurrentEmail(authentication);
        EnrollmentResponse response = projectService.enrollVolunteer(projectId, currentEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}/enrollment-status")
    public ResponseEntity<Boolean> isEnrolled(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        String currentEmail = getCurrentEmail(authentication);
        return ResponseEntity.ok(projectService.isVolunteerEnrolled(projectId, currentEmail));
    }

    private String getCurrentEmail(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated request");
        }
        return authentication.getName();
    }
}

