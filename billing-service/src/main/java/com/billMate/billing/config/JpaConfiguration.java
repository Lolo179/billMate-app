package com.billMate.billing.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuración de JPA para la aplicación
 * Se carga automáticamente solo cuando hay un contexto de aplicación completo (no en tests unitarios)
 */
@Configuration
@EntityScan("com.billMate.billing.entity")
@EnableJpaRepositories("com.billMate.billing.repository")
public class JpaConfiguration {
}
