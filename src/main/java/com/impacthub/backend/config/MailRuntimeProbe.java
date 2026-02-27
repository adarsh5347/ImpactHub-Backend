package com.impacthub.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailRuntimeProbe {

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    @EventListener(ApplicationReadyEvent.class)
    public void logMailRuntimeSettings() {
        log.info("Mail runtime probe: spring.mail.username={}", mailUsername);
        log.info("Mail runtime probe: spring.mail.password.present={}", mailPassword != null && !mailPassword.isBlank());
        log.info("Mail runtime probe: mail.enabled={}", mailEnabled);
    }
}
