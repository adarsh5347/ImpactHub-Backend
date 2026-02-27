package com.impacthub.backend.controller;

import com.impacthub.backend.dto.request.VolunteerUpdateRequest;
import com.impacthub.backend.dto.response.ActivityResponse;
import com.impacthub.backend.dto.response.VolunteerResponse;
import com.impacthub.backend.dto.response.VolunteerStatsResponse;
import com.impacthub.backend.service.VolunteerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/volunteers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    @GetMapping
    public ResponseEntity<List<VolunteerResponse>> listVolunteers() {
        return ResponseEntity.ok(volunteerService.listVolunteers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVolunteer(@PathVariable Long id, Authentication authentication) {
        String currentEmail = getCurrentEmail(authentication);

        boolean isOwner = volunteerService.isVolunteerOwnedByEmail(id, currentEmail);
        if (!isOwner) {
            return ResponseEntity.status(403).body("You can only view your own profile");
        }

        VolunteerResponse volunteer = volunteerService.getVolunteerById(id);
        return ResponseEntity.ok(volunteer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVolunteer(
            @PathVariable Long id,
            @RequestBody VolunteerUpdateRequest request,
            Authentication authentication
    ) {
        String currentEmail = getCurrentEmail(authentication);

        boolean isOwner = volunteerService.isVolunteerOwnedByEmail(id, currentEmail);
        if (!isOwner) {
            return ResponseEntity.status(403).body("You can only update your own profile");
        }

        VolunteerResponse updated = volunteerService.updateVolunteer(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVolunteer(@PathVariable Long id, Authentication authentication) {
        String currentEmail = getCurrentEmail(authentication);

        boolean isOwner = volunteerService.isVolunteerOwnedByEmail(id, currentEmail);
        if (!isOwner) {
            return ResponseEntity.status(403).body("You can only delete your own account");
        }

        volunteerService.deleteVolunteer(id);
        return ResponseEntity.ok("Account deleted successfully");
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<?> getActivities(@PathVariable Long id, Authentication authentication) {
        String currentEmail = getCurrentEmail(authentication);

        boolean isOwner = volunteerService.isVolunteerOwnedByEmail(id, currentEmail);
        if (!isOwner) {
            return ResponseEntity.status(403).body("You can only view your own activities");
        }

        List<ActivityResponse> activities = volunteerService.getActivities(id);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<VolunteerStatsResponse> getVolunteerStats(@PathVariable Long id) {
        return ResponseEntity.ok(volunteerService.getVolunteerStats(id));
    }

    @GetMapping("/me")
    public ResponseEntity<VolunteerResponse> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        VolunteerResponse volunteer = volunteerService.getVolunteerByEmail(email);
        return ResponseEntity.ok(volunteer);
    }

    private String getCurrentEmail(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated request");
        }
        return authentication.getName();
    }
}
