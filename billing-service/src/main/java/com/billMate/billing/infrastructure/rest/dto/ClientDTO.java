package com.billMate.billing.infrastructure.rest.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ClientDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-26T00:11:19.317367700+01:00[Europe/Madrid]", comments = "Generator version: 7.21.0")
public class ClientDTO {

  private String name;

  private String email;

  private @Nullable String phone;

  private String nif;

  private String address;

  private Long clientId;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime createdAt;

  public ClientDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ClientDTO(String name, String email, String nif, String address, Long clientId) {
    this.name = name;
    this.email = email;
    this.nif = nif;
    this.address = address;
    this.clientId = clientId;
  }

  public ClientDTO name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @NotNull @Size(min = 2, max = 100) 
  @Schema(name = "name", example = "Juan Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public ClientDTO email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
   */
  @NotNull @jakarta.validation.constraints.Email 
  @Schema(name = "email", example = "juanperez@mail.com", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(String email) {
    this.email = email;
  }

  public ClientDTO phone(@Nullable String phone) {
    this.phone = phone;
    return this;
  }

  /**
   * Get phone
   * @return phone
   */
  @Size(max = 20) 
  @Schema(name = "phone", example = "+34 600 123 456", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("phone")
  public @Nullable String getPhone() {
    return phone;
  }

  @JsonProperty("phone")
  public void setPhone(@Nullable String phone) {
    this.phone = phone;
  }

  public ClientDTO nif(String nif) {
    this.nif = nif;
    return this;
  }

  /**
   * Get nif
   * @return nif
   */
  @NotNull @Pattern(regexp = "^\\d{8}[A-Z]$") @Size(min = 9, max = 9) 
  @Schema(name = "nif", example = "12345678Z", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("nif")
  public String getNif() {
    return nif;
  }

  @JsonProperty("nif")
  public void setNif(String nif) {
    this.nif = nif;
  }

  public ClientDTO address(String address) {
    this.address = address;
    return this;
  }

  /**
   * Get address
   * @return address
   */
  @NotNull @Size(min = 5) 
  @Schema(name = "address", example = "Calle Falsa 123, Madrid", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("address")
  public String getAddress() {
    return address;
  }

  @JsonProperty("address")
  public void setAddress(String address) {
    this.address = address;
  }

  public ClientDTO clientId(Long clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Get clientId
   * @return clientId
   */
  @NotNull 
  @Schema(name = "clientId", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("clientId")
  public Long getClientId() {
    return clientId;
  }

  @JsonProperty("clientId")
  public void setClientId(Long clientId) {
    this.clientId = clientId;
  }

  public ClientDTO createdAt(@Nullable OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
   */
  @Valid 
  @Schema(name = "createdAt", example = "2025-05-15T14:30Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("createdAt")
  public @Nullable OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(@Nullable OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClientDTO clientDTO = (ClientDTO) o;
    return Objects.equals(this.name, clientDTO.name) &&
        Objects.equals(this.email, clientDTO.email) &&
        Objects.equals(this.phone, clientDTO.phone) &&
        Objects.equals(this.nif, clientDTO.nif) &&
        Objects.equals(this.address, clientDTO.address) &&
        Objects.equals(this.clientId, clientDTO.clientId) &&
        Objects.equals(this.createdAt, clientDTO.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, email, phone, nif, address, clientId, createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClientDTO {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    nif: ").append(toIndentedString(nif)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(@Nullable Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}

