package com.impacthub.backend.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Value("${app.logging.http.enabled:true}")
    private boolean httpLoggingEnabled;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String method = safeValue(request.getMethod());
        String uri = safeValue(request.getRequestURI());
        String query = request.getQueryString();
        String traceId = resolveTraceId(request);

        try {
            try {
                MDC.put(TRACE_ID_MDC_KEY, traceId);
            } catch (Exception ignored) {
            }

            try {
                response.setHeader(TRACE_ID_HEADER, traceId);
            } catch (Exception ignored) {
            }

            if (httpLoggingEnabled) {
                try {
                    String origin = safeValue(request.getHeader("Origin"));
                    String userAgent = safeValue(request.getHeader("User-Agent"));
                    String ip = getClientIp(request);
                    String fullPath = (query == null || query.isBlank()) ? uri : uri + "?" + query;
                    log.info("IN  {} {} origin={} ip={} ua={}", method, fullPath, origin, ip, userAgent);
                } catch (Exception ignored) {
                }
            }

            try {
                filterChain.doFilter(request, response);
            } finally {
                long durationMs = System.currentTimeMillis() - startTime;
                if (httpLoggingEnabled) {
                    try {
                        log.info("OUT {} {} {} durationMs={}", response.getStatus(), method, uri, durationMs);
                    } catch (Exception ignored) {
                    }
                }
            }
        } finally {
            try {
                MDC.remove(TRACE_ID_MDC_KEY);
            } catch (Exception ignored) {
            }
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        try {
            String incomingTraceId = request.getHeader(TRACE_ID_HEADER);
            if (incomingTraceId != null) {
                String trimmed = incomingTraceId.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
        } catch (Exception ignored) {
        }

        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String getClientIp(HttpServletRequest request) {
        try {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",")[0].trim();
            }
            return safeValue(request.getRemoteAddr());
        } catch (Exception ignored) {
            return "unknown";
        }
    }

    private String safeValue(String value) {
        return value == null ? "-" : value;
    }
}
