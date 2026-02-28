package com.impacthub.backend.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResendEmailService {

    private static final String RESEND_SEND_URL = "https://api.resend.com/emails";

    private final WebClient.Builder webClientBuilder;

    @Value("${resend.api-key:${RESEND_API_KEY:}}")
    private String resendApiKey;

    @Value("${resend.from-email:${RESEND_FROM_EMAIL:onboarding@resend.dev}}")
    private String resendFromEmail;

    public String sendEmail(String recipientEmail, String subject, String html) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new IllegalStateException("RESEND_API_KEY is not configured");
        }

        WebClient webClient = webClientBuilder.build();
        Map<String, Object> requestBody = Map.of(
                "from", resendFromEmail,
                "to", List.of(recipientEmail),
                "subject", subject,
                "html", html
        );

        log.info("Resend API request started: to={}, subject={}", recipientEmail, subject);
        String response = webClient.post()
                .uri(RESEND_SEND_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + resendApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("Resend returned error without body")
                                .map(body -> new RuntimeException("Resend API error: " + body)))
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(15));

        log.info("Resend API request completed: to={}, subject={}", recipientEmail, subject);
        return response;
    }
}
