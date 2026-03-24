package com.billMate.billing.infrastructure.idempotency;

/**
 * Almacena la respuesta cacheada de una operación idempotente.
 * Contiene el estado HTTP, el cuerpo serializado y el Content-Type.
 */
public record IdempotencyRecord(int status, byte[] body, String contentType) {
}
