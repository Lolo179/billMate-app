package com.billMate.billing.infrastructure.rest.dto;

import java.net.URI;
import java.util.Objects;
import com.billMate.billing.infrastructure.rest.dto.InvoiceLine;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
 * InvoiceDTO
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-26T00:11:19.317367700+01:00[Europe/Madrid]", comments = "Generator version: 7.21.0")
public class InvoiceDTO {

  private Long invoiceId;

  private Long clientId;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate date;

  @Valid
  private List<@Valid InvoiceLine> invoiceLines = new ArrayList<>();

  private @Nullable BigDecimal total;

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    DRAFT("DRAFT"),
    
    SENT("SENT"),
    
    PAID("PAID"),
    
    CANCELLED("CANCELLED");

    private final String value;

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

  private @Nullable StatusEnum status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime createdAt;

  private @Nullable Double taxPercentage;

  private @Nullable String description;

  public InvoiceDTO() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public InvoiceDTO(Long invoiceId, Long clientId, LocalDate date, List<@Valid InvoiceLine> invoiceLines) {
    this.invoiceId = invoiceId;
    this.clientId = clientId;
    this.date = date;
    this.invoiceLines = invoiceLines;
  }

  public InvoiceDTO invoiceId(Long invoiceId) {
    this.invoiceId = invoiceId;
    return this;
  }

  /**
   * Get invoiceId
   * @return invoiceId
   */
  @NotNull 
  @Schema(name = "invoiceId", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("invoiceId")
  public Long getInvoiceId() {
    return invoiceId;
  }

  @JsonProperty("invoiceId")
  public void setInvoiceId(Long invoiceId) {
    this.invoiceId = invoiceId;
  }

  public InvoiceDTO clientId(Long clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Get clientId
   * @return clientId
   */
  @NotNull 
  @Schema(name = "clientId", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("clientId")
  public Long getClientId() {
    return clientId;
  }

  @JsonProperty("clientId")
  public void setClientId(Long clientId) {
    this.clientId = clientId;
  }

  public InvoiceDTO date(LocalDate date) {
    this.date = date;
    return this;
  }

  /**
   * Get date
   * @return date
   */
  @NotNull @Valid 
  @Schema(name = "date", example = "2025-06-01", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("date")
  public LocalDate getDate() {
    return date;
  }

  @JsonProperty("date")
  public void setDate(LocalDate date) {
    this.date = date;
  }

  public InvoiceDTO invoiceLines(List<@Valid InvoiceLine> invoiceLines) {
    this.invoiceLines = invoiceLines;
    return this;
  }

  public InvoiceDTO addInvoiceLinesItem(InvoiceLine invoiceLinesItem) {
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
  @NotNull @Valid 
  @Schema(name = "invoiceLines", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("invoiceLines")
  public List<@Valid InvoiceLine> getInvoiceLines() {
    return invoiceLines;
  }

  @JsonProperty("invoiceLines")
  public void setInvoiceLines(List<@Valid InvoiceLine> invoiceLines) {
    this.invoiceLines = invoiceLines;
  }

  public InvoiceDTO total(@Nullable BigDecimal total) {
    this.total = total;
    return this;
  }

  /**
   * Get total
   * @return total
   */
  @Valid 
  @Schema(name = "total", accessMode = Schema.AccessMode.READ_ONLY, example = "1500.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("total")
  public @Nullable BigDecimal getTotal() {
    return total;
  }

  @JsonProperty("total")
  public void setTotal(@Nullable BigDecimal total) {
    this.total = total;
  }

  public InvoiceDTO status(@Nullable StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  
  @Schema(name = "status", example = "DRAFT", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable StatusEnum getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable StatusEnum status) {
    this.status = status;
  }

  public InvoiceDTO createdAt(@Nullable OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
   */
  @Valid 
  @Schema(name = "createdAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("createdAt")
  public @Nullable OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(@Nullable OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public InvoiceDTO taxPercentage(@Nullable Double taxPercentage) {
    this.taxPercentage = taxPercentage;
    return this;
  }

  /**
   * Get taxPercentage
   * @return taxPercentage
   */
  
  @Schema(name = "taxPercentage", accessMode = Schema.AccessMode.READ_ONLY, example = "21", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("taxPercentage")
  public @Nullable Double getTaxPercentage() {
    return taxPercentage;
  }

  @JsonProperty("taxPercentage")
  public void setTaxPercentage(@Nullable Double taxPercentage) {
    this.taxPercentage = taxPercentage;
  }

  public InvoiceDTO description(@Nullable String description) {
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
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(@Nullable String description) {
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
    InvoiceDTO invoiceDTO = (InvoiceDTO) o;
    return Objects.equals(this.invoiceId, invoiceDTO.invoiceId) &&
        Objects.equals(this.clientId, invoiceDTO.clientId) &&
        Objects.equals(this.date, invoiceDTO.date) &&
        Objects.equals(this.invoiceLines, invoiceDTO.invoiceLines) &&
        Objects.equals(this.total, invoiceDTO.total) &&
        Objects.equals(this.status, invoiceDTO.status) &&
        Objects.equals(this.createdAt, invoiceDTO.createdAt) &&
        Objects.equals(this.taxPercentage, invoiceDTO.taxPercentage) &&
        Objects.equals(this.description, invoiceDTO.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(invoiceId, clientId, date, invoiceLines, total, status, createdAt, taxPercentage, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InvoiceDTO {\n");
    sb.append("    invoiceId: ").append(toIndentedString(invoiceId)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    invoiceLines: ").append(toIndentedString(invoiceLines)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    taxPercentage: ").append(toIndentedString(taxPercentage)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

