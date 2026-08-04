package com.ecommerce.product.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * ===================================================================================
 * CORRELATION ID & MDC LOGGING FILTER:
 * ===================================================================================
 * 1. WHY IT EXISTS:
 *    In distributed microservices, a single client request triggers multiple HTTP calls across services.
 *    Without a shared Correlation ID passed in HTTP headers, tracking logs across services during an Incident is impossible.
 *
 * 2. MDC (Mapped Diagnostic Context):
 *    SLF4J MDC uses ThreadLocal storage to associate key-value pairs (like correlationId) with log lines.
 *    Any log message emitted during the request thread automatically includes the correlationId.
 *
 * 3. COMMON MISTAKES DEVELOPERS MAKE:
 *    - Forgetting to remove values from MDC in a `finally` block. Because servlet containers reuse thread pools,
 *      leaked MDC data will attach old correlation IDs to completely unrelated future user requests!
 * ===================================================================================
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Crucial: Clear MDC to prevent ThreadLocal memory leaks & log contamination across thread pool reuses
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}
