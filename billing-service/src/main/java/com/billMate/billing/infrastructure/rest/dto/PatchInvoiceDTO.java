package com.billMate.billing.infrastructure.rest.dto;

import java.net.URI;
import java.util.Objects;
import com.billMate.billing.infrastructure.rest.dto.InvoiceLine;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * DTO para actualización parcial de factura (RFC 7396 JSON Merge Patch). Solo válido en estado DRAFT. Solo los campos presentes en el body serán actualizados.
 */

@Schema(name = "PatchInvoiceDTO", description = "DTO para actualización parcial de factura (RFC 7396 JSON Merge Patch). Solo válido en estado DRAFT. Solo los campos presentes en el body serán actualizados.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-26T00:41:50.907954936Z[Etc/UTC]")
public class PatchInvoiceDTO {

  private Long clientId;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate date;

  private String description;

  @Valid
  private List<@Valid InvoiceLine> invoiceLines;

  public PatchInvoiceDTO clientId(Long clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Get clientId
   * minimum: 1
   * @return clientId
  */
  @Min(1L) 
  @Schema(name = "clientId", example = "3", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("clientId")
  public Long getClientId() {
    return clientId;
  }

  public void setClientId(Long clientId) {
    this.clientId = clientId;
  }

  public PatchInvoiceDTO date(LocalDate date) {
    this.date = date;
    return this;
  }

  /**
   * Get date
   * @return date
  */
  @Valid 
  @Schema(name = "date", example = "Sun Jun 01 00:00:00 UTC 2025", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("date")
  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public PatchInvoiceDTO description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
  */
  @Size(max = 255) 
  @Schema(name = "description", example = "Proyecto web actualizado", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public PatchInvoiceDTO invoiceLines(List<@Valid InvoiceLine> invoiceLines) {
    this.invoiceLines = invoiceLines;
    return this;
  }

  public PatchInvoiceDTO addInvoiceLinesItem(InvoiceLine invoiceLinesItem) {
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
  @Valid 
  @Schema(name = "invoiceLines", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("invoiceLines")
  public List<@Valid InvoiceLine> getInvoiceLines() {
    return invoiceLines;
  }

  public void setInvoiceLines(List<@Valid InvoiceLine> invoiceLines) {
    this.invoiceLines = invoiceLines;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatchInvoiceDTO patchInvoiceDTO = (PatchInvoiceDTO) o;
    return Objects.equals(this.clientId, patchInvoiceDTO.clientId) &&
        Objects.equals(this.date, patchInvoiceDTO.date) &&
        Objects.equals(this.description, patchInvoiceDTO.description) &&
        Objects.equals(this.invoiceLines, patchInvoiceDTO.invoiceLines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, date, description, invoiceLines);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PatchInvoiceDTO {\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    date: ").append(toIndentedString(date)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    invoiceLines: ").append(toIndentedString(invoiceLines)).append("\n");
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

