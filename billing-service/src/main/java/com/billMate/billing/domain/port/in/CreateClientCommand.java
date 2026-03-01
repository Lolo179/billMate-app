package com.billMate.billing.domain.port.in;

public record CreateClientCommand(
        String name,
        String email,
        String phone,
        String nif,
        String address
) {
    public CreateClientCommand {
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
