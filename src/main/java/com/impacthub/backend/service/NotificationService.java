package com.impacthub.backend.service;

import com.impacthub.backend.entity.NGO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;

    @Async
    public void notifyNgoApproved(NGO ngo) {
        log.info("Async approval email dispatch started for ngoId={}", ngo.getId());
        try {
            emailService.sendNgoApprovedEmail(ngo.getEmail(), ngo.getNgoName());
        } catch (Exception ex) {
            log.error("Failed to send approval email for NGO {}", ngo.getId(), ex);
        }
    }

    @Async
    public void notifyNgoRejected(NGO ngo, String reason) {
        log.info("Async rejection email dispatch started for ngoId={}", ngo.getId());
        try {
            emailService.sendNgoRejectedEmail(ngo.getEmail(), ngo.getNgoName(), reason);
        } catch (Exception ex) {
            log.error("Failed to send rejection email for NGO {}", ngo.getId(), ex);
        }
    }

    @Async
    public void notifyNgoSuspended(NGO ngo, String reason) {
        log.info("Async suspension email dispatch started for ngoId={}", ngo.getId());
        try {
            emailService.sendNgoSuspendedEmail(ngo.getEmail(), ngo.getNgoName(), reason);
        } catch (Exception ex) {
            log.error("Failed to send suspension email for NGO {}", ngo.getId(), ex);
        }
    }
}
