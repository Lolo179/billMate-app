package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.domain.invoice.port.in.CreateInvoiceCommand;
import com.billMate.billing.domain.invoice.port.in.CreateInvoiceUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.invoice.event.InvoiceCreatedEvent;
import com.billMate.billing.domain.invoice.port.out.InvoiceEventPublisherPort;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class CreateInvoiceService implements CreateInvoiceUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateInvoiceService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final InvoiceEventPublisherPort invoiceEventPublisherPort;

    public CreateInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort,
                                ClientRepositoryPort clientRepositoryPort,
                                InvoiceEventPublisherPort invoiceEventPublisherPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.invoiceEventPublisherPort = invoiceEventPublisherPort;
    }

    @Override
    public Invoice execute(CreateInvoiceCommand command) {
        log.info("Creating invoice", kv("clientId", command.clientId()), kv("lines", command.lines().size()));
        clientRepositoryPort.findById(command.clientId())
                .orElseThrow(() -> {
                    log.warn("Client not found", kv("clientId", command.clientId()));
                    return new EntityNotFoundException(
                        "Cliente con ID " + command.clientId() + " no encontrado.");
                });

        List<InvoiceLineItem> lines = new ArrayList<>();
        for (CreateInvoiceCommand.LineCommand lineCmd : command.lines()) {
            BigDecimal price = BigDecimal.valueOf(lineCmd.unitPrice());
            BigDecimal quantity = BigDecimal.valueOf(lineCmd.quantity());
            BigDecimal lineTotal = price.multiply(quantity);

            lines.add(new InvoiceLineItem(null, lineCmd.description(), quantity, price, lineTotal));
        }

        InvoiceStatus status = command.status() != null
                ? InvoiceStatus.valueOf(command.status())
                : InvoiceStatus.DRAFT;

        Invoice invoice = new Invoice(
                null,
                command.clientId(),
                lines,
                command.date(),
                status,
                command.description(),
                BigDecimal.ZERO,
                BigDecimal.valueOf(21),
                LocalDateTime.now()
        );
        invoice.recalculateTotal();

        Invoice saved = invoiceRepositoryPort.save(invoice);
        log.info("Invoice created", kv("invoiceId", saved.getId()), kv("total", saved.getTotal()), kv("status", saved.getStatus()));

        try {
            invoiceEventPublisherPort.publishInvoiceCreated(new InvoiceCreatedEvent(
                    saved.getId(),
                    saved.getClientId(),
                    saved.getDate(),
                    saved.getStatus().name(),
                    saved.getDescription(),
                    saved.getTotal(),
                    saved.getTaxPercentage(),
                    saved.getCreatedAt()
            ));
        } catch (Exception e) {
            log.error("Failed to publish invoice created event", kv("invoiceId", saved.getId()), e);
        }

        return saved;
    }
}
