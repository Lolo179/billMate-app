package com.billMate.billing.infrastructure.rest.dto;

import java.net.URI;
import java.util.Objects;
import com.billMate.billing.infrastructure.rest.dto.InvoiceLine;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * NewInvoiceDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-02T01:11:33.737203500+01:00[Europe/Madrid]")
public class NewInvoiceDTO {

  private Long clientId;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate date;

  @Valid
  private List<@Valid InvoiceLine> invoiceLines = new ArrayList<>();

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    DRAFT("DRAFT"),
    
    SENT("SENT"),
    
    PAID("PAID"),
    
    CANCELLED("CANCELLED");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private StatusEnum status = StatusEnum.DRAFT;

  private String description;

  public NewInvoiceDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public NewInvoiceDTO(Long clientId, LocalDate date, List<@Valid InvoiceLine> invoiceLines) {
    this.clientId = clientId;
    this.date = date;
    this.invoiceLines = invoiceLines;
  }

  public NewInvoiceDTO clientId(Long clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Get clientId
   * minimum: 1
   * @return clientId
  */
  @NotNull @Min(1L) 
  @Schema(name = "clientId", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("clientId")
  public Long getClientId() {
    return clientId;
  }

  public void setClientId(Long clientId) {
    this.clientId = clientId;
  }

  public NewInvoiceDTO date(LocalDate date) {
    this.date = date;
    return this;
  }

  /**
   * Get date
   * @return date
  */
  @NotNull @Valid 
  @Schema(name = "date", example = "Sun Jun 01 02:00:00 CEST 2025", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("date")
  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public NewInvoiceDTO invoiceLines(List<@Valid InvoiceLine> invoiceLines) {
    this.invoiceLines = invoiceLines;
    return this;
  }

  public NewInvoiceDTO addInvoiceLinesItem(InvoiceLine invoiceLinesItem) {
    if (this.invoiceLines == null) {
      this.invoiceLines = new ArrayList<>();
    }
    this.invoiceLines.add(invoiceLinesItem);
    return this;
  }

  /**
   * Get invoiceLines
   * @return invoiceLines
  */
  @NotNull @Valid @Size(min = 1) 
  @Schema(name = "invoiceLines", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("invoiceLines")
  public List<@Valid InvoiceLine> getInvoiceLines() {
    return invoiceLines;
  }

  public void setInvoiceLines(List<@Valid InvoiceLine> invoiceLines) {
    this.invoiceLines = invoiceLines;
  }

  public NewInvoiceDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public NewInvoiceDTO description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
  */
  @Size(max = 255) 
  @Schema(name = "description", example = "Proyecto web completo", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NewInvoiceDTO newInvoiceDTO = (NewInvoiceDTO) o;
    return Objects.equals(this.clientId, newInvoiceDTO.clientId) &&
        Objects.equals(this.date, newInvoiceDTO.date) &&
        Objects.equals(this.invoiceLines, newInvoiceDTO.invoiceLines) &&
        Objects.equals(this.status, newInvoiceDTO.status) &&
        Objects.equals(this.description, newInvoiceDTO.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, date, invoiceLines, status, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NewInvoiceDTO {\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    invoiceLines: ").append(toIndentedString(invoiceLines)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

