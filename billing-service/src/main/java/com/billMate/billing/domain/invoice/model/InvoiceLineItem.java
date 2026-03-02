package com.billMate.billing.domain.invoice.model;

import java.math.BigDecimal;

public class InvoiceLineItem {

    private Long id;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;

    public InvoiceLineItem() {
    }

    public InvoiceLineItem(Long id, String description, BigDecimal quantity,
                           BigDecimal unitPrice, BigDecimal total) {
        validate(description, quantity, unitPrice);
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = total != null ? total : quantity.multiply(unitPrice);
    }

    private void validate(String description, BigDecimal quantity, BigDecimal unitPrice) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Cada línea debe tener una descripción.");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Precio y cantidad deben ser mayores a cero.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Precio y cantidad deben ser mayores a cero.");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
