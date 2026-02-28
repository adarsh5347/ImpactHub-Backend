package com.impacthub.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final ResendEmailService resendEmailService;

    @Value("${mail.enabled:${MAIL_ENABLED:true}}")
    private boolean mailEnabled;

    @Value("${app.login.url:}")
    private String appLoginUrl;

    public boolean isMailEnabled() {
        return mailEnabled;
    }

    public String sendWelcomeEmail(String recipientEmail, String recipientName) {
        if (!mailEnabled) {
            log.info("Mail disabled, skipping send");
            return "mail-disabled";
        }

        String safeName = (recipientName == null || recipientName.isBlank()) ? "User" : recipientName;
        String html = """
                <p>Hi %s,</p>
                <p>Welcome to ImpactHub. Your account has been created successfully.</p>
                <p>You can now sign in and start using the platform.</p>
                <p>Regards,<br/>ImpactHub Team</p>
                """.formatted(safeName);

        return resendEmailService.sendEmail(recipientEmail, "Welcome to ImpactHub", html);
    }

    public String sendTestEmail(String recipientEmail) {
        if (!mailEnabled) {
            log.info("Mail disabled, skipping send");
            return "mail-disabled";
        }

        String html = """
                <h2>ImpactHub Mail Test</h2>
                <p>This is a test email from ImpactHub backend.</p>
                <p>If you received this, Resend API is configured correctly.</p>
                """;
        return resendEmailService.sendEmail(recipientEmail, "ImpactHub Resend API Test", html);
    }

    public String sendNgoApprovedEmail(String recipientEmail, String ngoName) {
        String safeName = (ngoName == null || ngoName.isBlank()) ? "NGO" : ngoName;
        String loginSection = (appLoginUrl == null || appLoginUrl.isBlank())
                ? ""
                : "<p>Login URL: <a href=\"" + appLoginUrl + "\">" + appLoginUrl + "</a></p>";
        String html = """
                <p>Hi %s,</p>
                <p>Welcome to ImpactHub.</p>
                <p>Your NGO account has been approved by the ImpactHub admin team.</p>
                <p>Registered NGO Email: %s</p>
                <p>You can now sign in and use the platform.</p>
                %s
                <p>Regards,<br/>ImpactHub Team</p>
                """.formatted(safeName, recipientEmail, loginSection);
        return sendNgoDecisionEmail(recipientEmail, "ImpactHub NGO Registration Approved", html);
    }

    public String sendNgoRejectedEmail(String recipientEmail, String ngoName, String reason) {
        String safeName = (ngoName == null || ngoName.isBlank()) ? "NGO" : ngoName;
        String safeReason = (reason == null || reason.isBlank()) ? "Not provided" : reason;
        String html = """
                <p>Hi %s,</p>
                <p>Your NGO account has been rejected by the ImpactHub admin team.</p>
                <p>Reason: %s</p>
                <p>Please update your details and contact support if needed.</p>
                <p>Regards,<br/>ImpactHub Team</p>
                """.formatted(safeName, safeReason);
        return sendNgoDecisionEmail(recipientEmail, "ImpactHub NGO Registration Rejected", html);
    }

    public String sendNgoSuspendedEmail(String recipientEmail, String ngoName, String reason) {
        String safeName = (ngoName == null || ngoName.isBlank()) ? "NGO" : ngoName;
        String safeReason = (reason == null || reason.isBlank()) ? "Not provided" : reason;
        String html = """
                <p>Hi %s,</p>
                <p>Your NGO account has been suspended by the ImpactHub admin team.</p>
                <p>Reason: %s</p>
                <p>Contact support for further assistance.</p>
                <p>Regards,<br/>ImpactHub Team</p>
                """.formatted(safeName, safeReason);
        return sendNgoDecisionEmail(recipientEmail, "ImpactHub NGO Account Suspended", html);
    }

    private String sendNgoDecisionEmail(String recipientEmail, String subject, String html) {
        if (!mailEnabled) {
            log.info("Mail disabled, skipping send");
            return "mail-disabled";
        }
        return resendEmailService.sendEmail(recipientEmail, subject, html);
    }
}
