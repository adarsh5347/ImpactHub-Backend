package com.impacthub.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String fromAddress;

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.login.url:}")
    private String appLoginUrl;

    public boolean isMailEnabled() {
        return mailEnabled;
    }

    public void sendWelcomeEmail(String recipientEmail, String recipientName) {
        if (!mailEnabled) {
            log.info("Mail disabled, skipping send");
            return;
        }

        String safeName = (recipientName == null || recipientName.isBlank()) ? "User" : recipientName;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject("Welcome to ImpactHub");
        message.setText(
                """
                Hi %s,

                Welcome to ImpactHub. Your account has been created successfully.

                You can now sign in and start using the platform.

                Regards,
                ImpactHub Team
                """.formatted(safeName)
        );

        log.info("Email sending started: type=welcome, to={}", recipientEmail);
        try {
            mailSender.send(message);
            log.info("Email sent successfully: type=welcome, to={}", recipientEmail);
        } catch (MailException ex) {
            log.error("Failed to send welcome email to {}", recipientEmail, ex);
        }
    }

    public void sendTestEmail(String recipientEmail) {
        if (!mailEnabled) {
            log.info("Mail disabled, skipping send");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject("ImpactHub SMTP Test");
        message.setText(
                """
                This is a test email from ImpactHub backend.
                If you received this, Gmail SMTP is configured correctly.
                """
        );

        log.info("Email sending started: type=smtp-test, to={}", recipientEmail);
        try {
            mailSender.send(message);
            log.info("Email sent successfully: type=smtp-test, to={}", recipientEmail);
        } catch (MailException ex) {
            log.error("Failed to send SMTP test email to {}", recipientEmail, ex);
        }
    }

    public void sendNgoApprovedEmail(String recipientEmail, String ngoName) {
        String safeName = (ngoName == null || ngoName.isBlank()) ? "NGO" : ngoName;
        String loginSection = (appLoginUrl == null || appLoginUrl.isBlank())
                ? ""
                : "Login URL: %s%n%n".formatted(appLoginUrl);
        sendNgoDecisionEmail(
                recipientEmail,
                "ImpactHub NGO Registration Approved",
                """
                Hi %s,

                Welcome to ImpactHub.
                Your NGO account has been approved by the ImpactHub admin team.

                Registered NGO Email: %s
                You can now sign in and use the platform.

                %s
                Regards,
                ImpactHub Team
                """.formatted(safeName, recipientEmail, loginSection)
        );
    }

    public void sendNgoRejectedEmail(String recipientEmail, String ngoName, String reason) {
        String safeName = (ngoName == null || ngoName.isBlank()) ? "NGO" : ngoName;
        String safeReason = (reason == null || reason.isBlank()) ? "Not provided" : reason;
        sendNgoDecisionEmail(
                recipientEmail,
                "ImpactHub NGO Registration Rejected",
                """
                Hi %s,

                Your NGO account has been rejected by the ImpactHub admin team.
                Reason: %s

                Please update your details and contact support if needed.

                Regards,
                ImpactHub Team
                """.formatted(safeName, safeReason)
        );
    }

    public void sendNgoSuspendedEmail(String recipientEmail, String ngoName, String reason) {
        String safeName = (ngoName == null || ngoName.isBlank()) ? "NGO" : ngoName;
        String safeReason = (reason == null || reason.isBlank()) ? "Not provided" : reason;
        sendNgoDecisionEmail(
                recipientEmail,
                "ImpactHub NGO Account Suspended",
                """
                Hi %s,

                Your NGO account has been suspended by the ImpactHub admin team.
                Reason: %s

                Contact support for further assistance.

                Regards,
                ImpactHub Team
                """.formatted(safeName, safeReason)
        );
    }

    private void sendNgoDecisionEmail(String recipientEmail, String subject, String body) {
        if (!mailEnabled) {
            log.info("Mail disabled, skipping send");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject(subject);
        message.setText(body);

        log.info("Email sending started: type=ngo-decision, to={}, subject={}", recipientEmail, subject);
        try {
            mailSender.send(message);
            log.info("Email sent successfully: type=ngo-decision, to={}, subject={}", recipientEmail, subject);
        } catch (MailException ex) {
            log.error("Failed to send NGO decision email to {}", recipientEmail, ex);
        }
    }
}
