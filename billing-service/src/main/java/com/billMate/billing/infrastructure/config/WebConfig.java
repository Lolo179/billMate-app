package com.billMate.billing.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * Configuración web adicional del servicio de billing.
 * <p>
 * {@link ShallowEtagHeaderFilter} añade soporte automático de ETags a todas las respuestas GET:
 * calcula un MD5 del cuerpo de respuesta, lo incluye en la cabecera {@code ETag} y devuelve
 * {@code 304 Not Modified} cuando el cliente envía {@code If-None-Match} con el mismo valor.
 * Reduce consumo de ancho de banda sin cambios en los controllers.
 */
@Configuration
public class WebConfig {

    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }
}
