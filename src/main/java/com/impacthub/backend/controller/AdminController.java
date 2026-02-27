package com.impacthub.backend.controller;

import com.impacthub.backend.dto.request.AdminNgoDecisionRequest;
import com.impacthub.backend.dto.response.AdminNgoDetailResponse;
import com.impacthub.backend.dto.response.AdminNgoListItemResponse;
import com.impacthub.backend.dto.response.AdminStatsResponse;
import com.impacthub.backend.dto.response.AdminVolunteerListItemResponse;
import com.impacthub.backend.dto.response.PaginatedResponse;
import com.impacthub.backend.entity.ApprovalStatus;
import com.impacthub.backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/ngos")
    public ResponseEntity<PaginatedResponse<AdminNgoListItemResponse>> listNgos(
            @RequestParam(defaultValue = "PENDING") ApprovalStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(adminService.listNgos(status, search, page, limit));
    }

    @GetMapping("/ngos/{ngoId}")
    public ResponseEntity<AdminNgoDetailResponse> getNgoDetails(@PathVariable Long ngoId) {
        return ResponseEntity.ok(adminService.getNgoDetails(ngoId));
    }

    @DeleteMapping("/ngos/{ngoId}")
    public ResponseEntity<Void> deleteNgo(@PathVariable Long ngoId) {
        adminService.deleteNgo(ngoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/ngos/{ngoId}/approve")
    public ResponseEntity<AdminNgoDetailResponse> approveNgo(@PathVariable Long ngoId, Authentication authentication) {
        return ResponseEntity.ok(adminService.approveNgo(ngoId, authentication.getName()));
    }

    @PostMapping("/ngos/{ngoId}/reject")
    public ResponseEntity<AdminNgoDetailResponse> rejectNgo(
            @PathVariable Long ngoId,
            @Valid @RequestBody AdminNgoDecisionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.rejectNgo(ngoId, authentication.getName(), request.getReason()));
    }

    @PostMapping("/ngos/{ngoId}/suspend")
    public ResponseEntity<AdminNgoDetailResponse> suspendNgo(
            @PathVariable Long ngoId,
            @Valid @RequestBody AdminNgoDecisionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.suspendNgo(ngoId, authentication.getName(), request.getReason()));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/volunteers")
    public ResponseEntity<PaginatedResponse<AdminVolunteerListItemResponse>> listVolunteers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(adminService.listVolunteers(search, page, limit));
    }
}
