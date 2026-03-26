package com.billMate.billing.infrastructure.config;

import com.billMate.billing.infrastructure.idempotency.IdempotencyFilter;
import com.billMate.billing.infrastructure.idempotency.IdempotencyStore;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra el filtro de idempotencia para todas las rutas de la API de billing.
 * El filtro solo actúa sobre peticiones POST con cabecera Idempotency-Key.
 */
@Configuration
public class IdempotencyConfig {

    @Bean
    public IdempotencyFilter idempotencyFilter(IdempotencyStore idempotencyStore,
                                               ObjectMapper objectMapper) {
        return new IdempotencyFilter(idempotencyStore, objectMapper);
    }

    @Bean
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilterRegistration(
            IdempotencyFilter idempotencyFilter) {
        FilterRegistrationBean<IdempotencyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(idempotencyFilter);
        registration.addUrlPatterns("/clients/*", "/clients", "/invoices/*", "/invoices");
        registration.setOrder(10);
        return registration;
    }
}
