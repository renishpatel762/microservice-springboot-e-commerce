package com.ecommerce.order.client;

import com.ecommerce.order.filter.CorrelationIdFilter;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * ===================================================================================
 * WEBCLIENT & DISTRIBUTED TRACING CONFIGURATION:
 * ===================================================================================
 * 1. WHY WEBCLIENT IS PREFERRED OVER RESTTEMPLATE:
 *    - RestTemplate is in maintenance mode in Spring 5+.
 *    - WebClient provides a functional, fluent API supporting both synchronous and asynchronous calls.
 *    - Built-in HTTP client engine (Netty/Reactor) with fine-grained connection pooling, read/write timeout control.
 *
 * 2. CORRELATION ID & AUTH PROPAGATION:
 *    The ExchangeFilterFunction extracts the active MDC `correlationId` and incoming `Authorization` Bearer token
 *    from the calling request and injects them into outgoing HTTP requests to Product Service.
 * ===================================================================================
 */
@Configuration
public class WebClientConfig {

    @Value("${product-service.base-url}")
    private String productServiceBaseUrl;

    @Value("${product-service.timeout.connect-ms:5000}")
    private int connectTimeoutMs;

    @Value("${product-service.timeout.read-ms:5000}")
    private int readTimeoutMs;

    @Bean
    public WebClient productServiceWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(readTimeoutMs))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(productServiceBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(headerForwardingFilter())
                .build();
    }

    private ExchangeFilterFunction headerForwardingFilter() {
        return (request, next) -> {
            ClientRequest.Builder builder = ClientRequest.from(request);

            String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
            if (correlationId != null && !correlationId.isBlank()) {
                builder.header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest servletRequest = attributes.getRequest();
                String authHeader = servletRequest.getHeader("Authorization");
                if (authHeader != null && !authHeader.isBlank()) {
                    builder.header("Authorization", authHeader);
                }
            }

            return next.exchange(builder.build());
        };
    }
}
