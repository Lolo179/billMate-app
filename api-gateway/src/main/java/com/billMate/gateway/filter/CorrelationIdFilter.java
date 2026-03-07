package com.billMate.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * GlobalFilter que genera un UUID de correlación para cada petición entrante.
 * Si la petición ya incluye el header x-Correlation-Id, lo reutiliza.
 * Propaga el valor en el header de request (downstream), response (upstream) y MDC (logging).
 */
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_HEADER = "x-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Correlation ID generated", kv("correlationId", correlationId));
        } else {
            log.debug("Correlation ID received", kv("correlationId", correlationId));
        }

        final String id = correlationId;

        // Propagar el header hacia los microservicios downstream y de vuelta al cliente
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder.header(CORRELATION_HEADER, id))
                .build();
        mutatedExchange.getResponse().getHeaders().add(CORRELATION_HEADER, id);

        return chain.filter(mutatedExchange)
                .doFirst(() -> MDC.put(MDC_KEY, id))
                .doFinally(signal -> MDC.remove(MDC_KEY));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
