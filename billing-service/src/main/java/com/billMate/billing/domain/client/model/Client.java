package com.billMate.billing.domain.client.model;

import java.time.OffsetDateTime;

public class Client {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String nif;
    private String address;
    private OffsetDateTime createdAt;

    public Client() {
    }

    public Client(Long id, String name, String email, String phone, String nif, String address, OffsetDateTime createdAt) {
        validate(name, email, nif);
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.nif = nif;
        this.address = address;
        this.createdAt = createdAt;
    }

    private void validate(String name, String email, String nif) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Client name is required");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Client name must not exceed 255 characters");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Client email is required");
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Client email format is invalid");
        }
        if (nif == null || nif.isBlank()) {
            throw new IllegalArgumentException("Client NIF is required");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Client name is required");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Client name must not exceed 255 characters");
        }
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Client email is required");
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Client email format is invalid");
        }
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        if (nif == null || nif.isBlank()) {
            throw new IllegalArgumentException("Client NIF is required");
        }
        this.nif = nif;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
