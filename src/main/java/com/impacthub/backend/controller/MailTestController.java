package com.impacthub.backend.controller;

import com.impacthub.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MailTestController {

    private final EmailService emailService;

    @GetMapping("/mail")
    public ResponseEntity<?> sendMailTest(@RequestParam("to") String to) {
        if (!emailService.isMailEnabled()) {
            return ResponseEntity.ok(Map.of(
                    "message", "Mail disabled, skipping send",
                    "to", to
            ));
        }

        try {
            emailService.sendTestEmail(to);
            return ResponseEntity.ok(Map.of(
                    "message", "Mail sent successfully",
                    "to", to
            ));
        } catch (MailException ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Mail send failed",
                    "to", to,
                    "error", ex.getMessage()
            ));
        }
    }
}
