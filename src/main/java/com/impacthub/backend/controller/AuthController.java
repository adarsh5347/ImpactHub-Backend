package com.impacthub.backend.controller;

import com.impacthub.backend.dto.request.LoginRequest;
import com.impacthub.backend.dto.request.NGORegistrationRequest;
import com.impacthub.backend.dto.request.VolunteerRegistrationRequest;
import com.impacthub.backend.dto.response.AuthMeResponse;
import com.impacthub.backend.dto.response.AuthResponse;
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/volunteer")
    public ResponseEntity<AuthResponse> registerVolunteer(
            @Valid @RequestBody VolunteerRegistrationRequest request) {
        AuthResponse response = authService.registerVolunteer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/ngo")
    public ResponseEntity<AuthResponse> registerNGO(
            @Valid @RequestBody NGORegistrationRequest request) {
        AuthResponse response = authService.registerNGO(request);
        return ResponseEntity.ok(response);
    }
}
