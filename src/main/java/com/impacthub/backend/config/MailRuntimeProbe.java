package com.impacthub.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailRuntimeProbe {

    @Value("${resend.api-key:${RESEND_API_KEY:}}")
    private String resendApiKey;

    @Value("${resend.from-email:${RESEND_FROM_EMAIL:onboarding@resend.dev}}")
    private String resendFromEmail;

    @Value("${mail.enabled:${MAIL_ENABLED:true}}")
    private boolean mailEnabled;

    @EventListener(ApplicationReadyEvent.class)
    public void logMailRuntimeSettings() {
        log.info("Mail runtime probe: resend.from-email={}", resendFromEmail);
        log.info("Mail runtime probe: resend.api-key.present={}", resendApiKey != null && !resendApiKey.isBlank());
        log.info("Mail runtime probe: mail.enabled={}", mailEnabled);
    }
}
