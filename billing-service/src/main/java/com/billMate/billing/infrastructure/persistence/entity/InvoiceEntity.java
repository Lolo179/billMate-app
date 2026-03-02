package com.billMate.billing.infrastructure.persistence.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.billMate.billing.domain.invoice.model.InvoiceStatus;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "invoices")
public class InvoiceEntity {

    @Id
    @Column(name = "invoice_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "client_id",nullable = false)
    private ClientEntity client;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLineEntity> invoiceLines = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxPercentage = BigDecimal.valueOf(21);

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public void recalculateTotal() {
        this.total = invoiceLines.stream()
                .map(InvoiceLineEntity::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
