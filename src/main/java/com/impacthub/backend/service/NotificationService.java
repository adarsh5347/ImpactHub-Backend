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
    public void sendVolunteerWelcomeEmail(String recipientEmail, String recipientName) {
        if (!isPresent(recipientEmail)) {
            log.warn("Skipping volunteer welcome email because recipient email is missing");
            return;
        }

        log.info("Async volunteer welcome email dispatch started for email={}", recipientEmail);
        try {
            emailService.sendWelcomeEmail(recipientEmail, recipientName);
            log.info("Volunteer welcome email queued for email={}", recipientEmail);
        } catch (Exception ex) {
            log.error("Failed to send volunteer welcome email for {}", recipientEmail, ex);
        }
    }

    @Async
    public void sendApprovalEmail(NGO ngo) {
        log.info("Async approval email dispatch started for ngoId={}", ngo.getId());
        try {
            String recipientEmail = resolveRecipientEmail(ngo);
            if (recipientEmail == null) {
                log.warn("Skipping approval email for ngoId={} because no recipient email is available", ngo.getId());
                return;
            }
            emailService.sendNgoApprovedEmail(recipientEmail, ngo.getNgoName());
            log.info("Approval email queued for ngoId={} to={}", ngo.getId(), recipientEmail);
        } catch (Exception ex) {
            log.error("Failed to send approval email for NGO {}", ngo.getId(), ex);
        }
    }

    @Async
    public void notifyNgoRejected(NGO ngo, String reason) {
        log.info("Async rejection email dispatch started for ngoId={}", ngo.getId());
        try {
            String recipientEmail = resolveRecipientEmail(ngo);
            if (recipientEmail == null) {
                log.warn("Skipping rejection email for ngoId={} because no recipient email is available", ngo.getId());
                return;
            }
            emailService.sendNgoRejectedEmail(recipientEmail, ngo.getNgoName(), reason);
            log.info("Rejection email queued for ngoId={} to={}", ngo.getId(), recipientEmail);
        } catch (Exception ex) {
            log.error("Failed to send rejection email for NGO {}", ngo.getId(), ex);
        }
    }

    @Async
    public void notifyNgoSuspended(NGO ngo, String reason) {
        log.info("Async suspension email dispatch started for ngoId={}", ngo.getId());
        try {
            String recipientEmail = resolveRecipientEmail(ngo);
            if (recipientEmail == null) {
                log.warn("Skipping suspension email for ngoId={} because no recipient email is available", ngo.getId());
                return;
            }
            emailService.sendNgoSuspendedEmail(recipientEmail, ngo.getNgoName(), reason);
            log.info("Suspension email queued for ngoId={} to={}", ngo.getId(), recipientEmail);
        } catch (Exception ex) {
            log.error("Failed to send suspension email for NGO {}", ngo.getId(), ex);
        }
    }

    private String resolveRecipientEmail(NGO ngo) {
        if (isPresent(ngo.getEmail())) {
            return ngo.getEmail();
        }
        if (ngo.getUser() != null && isPresent(ngo.getUser().getEmail())) {
            return ngo.getUser().getEmail();
        }
        if (isPresent(ngo.getPrimaryContactEmail())) {
            return ngo.getPrimaryContactEmail();
        }
        return null;
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
