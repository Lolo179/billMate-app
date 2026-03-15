package com.billMate.frontend;

/**
 * Clase legado mantenida temporalmente para no romper referencias locales.
 * El frontend actual es una SPA React/Vite y no arranca contexto de Spring.
 */
public final class FrontendServiceApplication {

    private FrontendServiceApplication() {
    }

    public static void main(String[] args) {
        throw new UnsupportedOperationException(
                "frontend-service ya no usa Spring Boot. Usa `npm run dev` o `npm run build`."
        );
    }
}
