package com.billMate.billing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Smoke test: verifica que el contexto de Spring arranca correctamente con todos
 * sus beans (incluyendo IdempotencyConfig, JpaConfiguration, etc.).
 * Usa Testcontainers (PostgreSQL 16-alpine) vía BillingIntegrationTestBase.
 */
class BillingServiceApplicationTests extends BillingIntegrationTestBase {

    @Test
    @DisplayName("Spring context loads successfully")
    void contextLoads() {
        // El contexto arranca correctamente si no se lanza ninguna excepción
    }
}
