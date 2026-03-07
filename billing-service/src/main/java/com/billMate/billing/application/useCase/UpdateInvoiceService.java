package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.domain.invoice.port.in.UpdateInvoiceCommand;
import com.billMate.billing.domain.invoice.port.in.UpdateInvoiceUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class UpdateInvoiceService implements UpdateInvoiceUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateInvoiceService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;

    public UpdateInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort,
                                ClientRepositoryPort clientRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public Invoice execute(UpdateInvoiceCommand command) {
        log.info("Updating invoice", kv("invoiceId", command.invoiceId()));
        Invoice invoice = invoiceRepositoryPort.findById(command.invoiceId())
                .orElseThrow(() -> {
                    log.warn("Invoice not found", kv("invoiceId", command.invoiceId()));
                    return new EntityNotFoundException("Factura no encontrada");
                });

        clientRepositoryPort.findById(command.clientId())
                .orElseThrow(() -> {
                    log.warn("Client not found", kv("clientId", command.clientId()));
                    return new EntityNotFoundException("Cliente no encontrado");
                });

        List<InvoiceLineItem> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (UpdateInvoiceCommand.LineCommand lineCmd : command.lines()) {
            BigDecimal price = BigDecimal.valueOf(lineCmd.unitPrice());
            BigDecimal quantity = BigDecimal.valueOf(lineCmd.quantity());
            BigDecimal lineTotal = price.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

            lines.add(new InvoiceLineItem(null, lineCmd.description(), quantity, price, lineTotal));
            total = total.add(lineTotal);
        }

        invoice.setClientId(command.clientId());
        invoice.setDate(command.date());
        invoice.setDescription(command.description());
        invoice.setStatus(InvoiceStatus.valueOf(command.status()));
        invoice.setLines(lines);
        invoice.setTotal(total.setScale(2, RoundingMode.HALF_UP));

        Invoice saved = invoiceRepositoryPort.save(invoice);
        log.info("Invoice updated", kv("invoiceId", saved.getId()), kv("total", saved.getTotal()));
        return saved;
    }
}
