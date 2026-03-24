package com.billMate.billing.infrastructure.idempotency;

import java.util.Optional;

/**
 * Puerto de salida para la caché de idempotencia.
 * Permite guardar y recuperar respuestas previamente procesadas por clave UUID.
 */
public interface IdempotencyStore {

    /**
     * Busca una respuesta cacheada para la clave dada.
     *
     * @param key UUID de idempotencia como String
     * @return respuesta cacheada si existe, vacío en caso contrario
     */
    Optional<IdempotencyRecord> get(String key);

    /**
     * Almacena la respuesta para que futuras peticiones con la misma clave
     * puedan recuperarla sin repetir el procesamiento.
     *
     * @param key    UUID de idempotencia como String
     * @param record datos de la respuesta a cachear
     */
    void save(String key, IdempotencyRecord record);
}
