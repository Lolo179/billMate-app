package com.billMate.gateway.filter;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

/**
 * Filtro reactivo que genera un UUID de correlación para cada petición entrante.
 * Si la petición ya incluye el header x-Correlation-Id, lo reutiliza.
 * Propaga el valor en el header de request (downstream), response (upstream) y MDC (logging).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements WebFilter {

    private static final String CORRELATION_HEADER = "x-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String id = correlationId;

        // Propagar el header hacia los microservicios downstream y de vuelta al cliente
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder.header(CORRELATION_HEADER, id))
                .build();
        mutatedExchange.getResponse().getHeaders().add(CORRELATION_HEADER, id);

        return chain.filter(mutatedExchange)
                .contextWrite(Context.of(CORRELATION_HEADER, id))
                .doFirst(() -> MDC.put(MDC_KEY, id))
                .doFinally(signal -> MDC.remove(MDC_KEY));
    }
}
