package com.billMate.billing.domain.client.port.in;

public record UpdateClientCommand(
        Long clientId,
        String name,
        String email,
        String phone,
        String nif,
        String address
) {
    public UpdateClientCommand {
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Client name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Client email is required");
        }
        if (nif == null || nif.isBlank()) {
            throw new IllegalArgumentException("Client NIF is required");
        }
    }
}
