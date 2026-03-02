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
 * NewClientDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-02T01:11:33.737203500+01:00[Europe/Madrid]")
public class NewClientDTO {

  private String name;

  private String email;

  private String phone;

  private String nif;

  private String address;

  public NewClientDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public NewClientDTO(String name, String email, String nif, String address) {
    this.name = name;
    this.email = email;
    this.nif = nif;
    this.address = address;
  }

  public NewClientDTO name(String name) {
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

  public void setName(String name) {
    this.name = name;
  }

  public NewClientDTO email(String email) {
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

  public void setEmail(String email) {
    this.email = email;
  }

  public NewClientDTO phone(String phone) {
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

  public NewClientDTO nif(String nif) {
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

  public void setNif(String nif) {
    this.nif = nif;
  }

  public NewClientDTO address(String address) {
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
    NewClientDTO newClientDTO = (NewClientDTO) o;
    return Objects.equals(this.name, newClientDTO.name) &&
        Objects.equals(this.email, newClientDTO.email) &&
        Objects.equals(this.phone, newClientDTO.phone) &&
        Objects.equals(this.nif, newClientDTO.nif) &&
        Objects.equals(this.address, newClientDTO.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, email, phone, nif, address);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NewClientDTO {\n");
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

