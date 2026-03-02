package com.billMate.billing.domain.invoice.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Invoice {

    private Long id;
    private Long clientId;
    private List<InvoiceLineItem> lines;
    private LocalDate date;
    private InvoiceStatus status;
    private String description;
    private BigDecimal total;
    private BigDecimal taxPercentage;
    private LocalDateTime createdAt;

    public Invoice() {
        this.lines = new ArrayList<>();
        this.status = InvoiceStatus.DRAFT;
        this.taxPercentage = BigDecimal.valueOf(21);
    }

    public Invoice(Long id, Long clientId, List<InvoiceLineItem> lines, LocalDate date,
                   InvoiceStatus status, String description, BigDecimal total,
                   BigDecimal taxPercentage, LocalDateTime createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.lines = lines != null ? lines : new ArrayList<>();
        this.date = date;
        this.status = status != null ? status : InvoiceStatus.DRAFT;
        this.description = description;
        this.total = total;
        this.taxPercentage = taxPercentage != null ? taxPercentage : BigDecimal.valueOf(21);
        this.createdAt = createdAt;
    }

    public void recalculateTotal() {
        BigDecimal subtotal = lines.stream()
                .map(InvoiceLineItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = subtotal.multiply(taxPercentage)
                .divide(BigDecimal.valueOf(100));
        this.total = subtotal.add(taxAmount);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public List<InvoiceLineItem> getLines() {
        return lines;
    }

    public void setLines(List<InvoiceLineItem> lines) {
        this.lines = lines;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
