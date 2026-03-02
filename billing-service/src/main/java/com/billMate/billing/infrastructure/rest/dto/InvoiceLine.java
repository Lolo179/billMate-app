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
 * InvoiceLine
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-02T01:11:33.737203500+01:00[Europe/Madrid]")
public class InvoiceLine {

  private String description;

  private Double quantity;

  private Double unitPrice;

  private Double total;

  public InvoiceLine() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public InvoiceLine(String description, Double quantity, Double unitPrice) {
    this.description = description;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
  }

  public InvoiceLine description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
  */
  @NotNull @Size(min = 3, max = 100) 
  @Schema(name = "description", example = "Desarrollo Web", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public InvoiceLine quantity(Double quantity) {
    this.quantity = quantity;
    return this;
  }

  /**
   * Get quantity
   * minimum: 0.01
   * @return quantity
  */
  @NotNull @DecimalMin("0.01") 
  @Schema(name = "quantity", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("quantity")
  public Double getQuantity() {
    return quantity;
  }

  public void setQuantity(Double quantity) {
    this.quantity = quantity;
  }

  public InvoiceLine unitPrice(Double unitPrice) {
    this.unitPrice = unitPrice;
    return this;
  }

  /**
   * Get unitPrice
   * minimum: 0.01
   * @return unitPrice
  */
  @NotNull @DecimalMin("0.01") 
  @Schema(name = "unitPrice", example = "100.0", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("unitPrice")
  public Double getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(Double unitPrice) {
    this.unitPrice = unitPrice;
  }

  public InvoiceLine total(Double total) {
    this.total = total;
    return this;
  }

  /**
   * Get total
   * @return total
  */
  
  @Schema(name = "total", accessMode = Schema.AccessMode.READ_ONLY, example = "500.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("total")
  public Double getTotal() {
    return total;
  }

  public void setTotal(Double total) {
    this.total = total;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InvoiceLine invoiceLine = (InvoiceLine) o;
    return Objects.equals(this.description, invoiceLine.description) &&
        Objects.equals(this.quantity, invoiceLine.quantity) &&
        Objects.equals(this.unitPrice, invoiceLine.unitPrice) &&
        Objects.equals(this.total, invoiceLine.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, quantity, unitPrice, total);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InvoiceLine {\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
    sb.append("    unitPrice: ").append(toIndentedString(unitPrice)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
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

