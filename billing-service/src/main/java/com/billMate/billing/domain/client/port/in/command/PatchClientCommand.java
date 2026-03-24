package com.billMate.billing.domain.client.port.in.command;

import java.util.Optional;

/**
 * Comando para actualización parcial de un cliente (JSON Merge Patch, RFC 7396).
 * Un Optional.empty() indica que el campo no está presente en el payload y no debe modificarse.
 * Un Optional.of(valor) indica que el campo debe actualizarse.
 */
public record PatchClientCommand(
        Long clientId,
        Optional<String> name,
        Optional<String> email,
        Optional<String> phone,
        Optional<String> nif,
        Optional<String> address) {
}
