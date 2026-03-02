package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.domain.invoice.port.in.CreateInvoiceCommand;
import com.billMate.billing.domain.invoice.port.in.CreateInvoiceUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreateInvoiceService implements CreateInvoiceUseCase {

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;

    public CreateInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort,
                                ClientRepositoryPort clientRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public Invoice execute(CreateInvoiceCommand command) {
        clientRepositoryPort.findById(command.clientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente con ID " + command.clientId() + " no encontrado."));

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

        return invoiceRepositoryPort.save(invoice);
    }
}
