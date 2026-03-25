package com.billMate.billing.infrastructure.rest.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * DTO para actualización parcial de cliente (RFC 7396 JSON Merge Patch). Solo los campos presentes en el body serán actualizados.
 */

@Schema(name = "PatchClientDTO", description = "DTO para actualización parcial de cliente (RFC 7396 JSON Merge Patch). Solo los campos presentes en el body serán actualizados.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-26T00:19:34.971748300+01:00[Europe/Madrid]", comments = "Generator version: 7.21.0")
public class PatchClientDTO {

  private @Nullable String name;

  private @Nullable String email;

  private @Nullable String phone;

  private @Nullable String nif;

  private @Nullable String address;

  public PatchClientDTO name(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @Size(min = 2, max = 100) 
  @Schema(name = "name", example = "Juan Pérez", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public @Nullable String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(@Nullable String name) {
    this.name = name;
  }

  public PatchClientDTO email(@Nullable String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
   */
  @jakarta.validation.constraints.Email 
  @Schema(name = "email", example = "juanperez@mail.com", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("email")
  public @Nullable String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(@Nullable String email) {
    this.email = email;
  }

  public PatchClientDTO phone(@Nullable String phone) {
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

  public PatchClientDTO nif(@Nullable String nif) {
    this.nif = nif;
    return this;
  }

  /**
   * Get nif
   * @return nif
   */
  @Pattern(regexp = "^\\d{8}[A-Z]$") @Size(min = 9, max = 9) 
  @Schema(name = "nif", example = "12345678Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("nif")
  public @Nullable String getNif() {
    return nif;
  }

  @JsonProperty("nif")
  public void setNif(@Nullable String nif) {
    this.nif = nif;
  }

  public PatchClientDTO address(@Nullable String address) {
    this.address = address;
    return this;
  }

  /**
   * Get address
   * @return address
   */
  
  @Schema(name = "address", example = "Calle Falsa 123, Madrid", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("address")
  public @Nullable String getAddress() {
    return address;
  }

  @JsonProperty("address")
  public void setAddress(@Nullable String address) {
    this.address = address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatchClientDTO patchClientDTO = (PatchClientDTO) o;
    return Objects.equals(this.name, patchClientDTO.name) &&
        Objects.equals(this.email, patchClientDTO.email) &&
        Objects.equals(this.phone, patchClientDTO.phone) &&
        Objects.equals(this.nif, patchClientDTO.nif) &&
        Objects.equals(this.address, patchClientDTO.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, email, phone, nif, address);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PatchClientDTO {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    nif: ").append(toIndentedString(nif)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
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

