package com.billMate.billing.infrastructure.rest.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-24T22:56:18.482070300+01:00[Europe/Madrid]")
public class PatchClientDTO {

  private String name;

  private String email;

  private String phone;

  private String nif;

  private String address;

  public PatchClientDTO name(String name) {
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
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PatchClientDTO email(String email) {
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
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public PatchClientDTO phone(String phone) {
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
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public PatchClientDTO nif(String nif) {
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
  public String getNif() {
    return nif;
  }

  public void setNif(String nif) {
    this.nif = nif;
  }

  public PatchClientDTO address(String address) {
    this.address = address;
    return this;
  }

  /**
   * Get address
   * @return address
  */
  
  @Schema(name = "address", example = "Calle Falsa 123, Madrid", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("address")
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
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
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

