package com.impacthub.backend.controller;

import com.impacthub.backend.dto.request.LoginRequest;
import com.impacthub.backend.dto.request.NGORegistrationRequest;
import com.impacthub.backend.dto.request.VolunteerRegistrationRequest;
import com.impacthub.backend.dto.response.AuthMeResponse;
import com.impacthub.backend.dto.response.AuthResponse;
import com.impacthub.backend.dto.response.NgoLoginBlockedResponse;
import com.impacthub.backend.exception.NgoLoginBlockedException;
import com.impacthub.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/test")
    public String test() {
        return "Backend working";
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated request");
        }
        return ResponseEntity.ok(authService.getCurrentUserProfile(authentication.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (NgoLoginBlockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new NgoLoginBlockedResponse(
                            e.getCode(),
                            e.getMessage(),
                            e.getNgoStatus(),
                            e.getRejectionReason(),
                            e.getSuspensionReason()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, e.getMessage()));
        }
    }

    @PostMapping("/register/volunteer")
    public ResponseEntity<AuthResponse> registerVolunteer(
            @Valid @RequestBody VolunteerRegistrationRequest request) {
        try {
            AuthResponse response = authService.registerVolunteer(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, e.getMessage()));
        }
    }

    @PostMapping("/register/ngo")
    public ResponseEntity<AuthResponse> registerNGO(
            @Valid @RequestBody NGORegistrationRequest request) {
        try {
            AuthResponse response = authService.registerNGO(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, e.getMessage()));
        }
    }
}
