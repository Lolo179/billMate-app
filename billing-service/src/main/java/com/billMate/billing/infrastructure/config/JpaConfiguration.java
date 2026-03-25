package com.billMate.billing.infrastructure.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuración de JPA para la aplicación
 * Se carga automáticamente solo cuando hay un contexto de aplicación completo (no en tests unitarios)
 */
@Configuration
@EntityScan("com.billMate.billing.infrastructure.persistence.entity")
@EnableJpaRepositories(
        "com.billMate.billing.infrastructure.persistence.repository"
)
public class JpaConfiguration {
}
