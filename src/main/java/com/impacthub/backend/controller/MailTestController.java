package com.impacthub.backend.controller;

import com.impacthub.backend.service.ResendEmailService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MailTestController {

    private final ResendEmailService resendEmailService;

    @GetMapping("/mail")
    public ResponseEntity<?> sendMailTest(@RequestParam("to") String to) {
        try {
            String providerResponse = resendEmailService.sendEmail(
                    to,
                    "ImpactHub Resend API Test",
                    "<h2>ImpactHub Mail Test</h2><p>Resend API integration is working.</p>"
            );
            return ResponseEntity.ok(Map.of(
                    "to", to,
                    "message", "sent",
                    "provider", "resend",
                    "providerResponse", providerResponse == null ? "" : providerResponse
            ));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "to", to,
                    "message", "failed",
                    "error", ex.getMessage()
            ));
        }
    }
}
