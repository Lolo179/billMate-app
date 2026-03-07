package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.port.in.DeleteInvoiceUseCase;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class DeleteInvoiceService implements DeleteInvoiceUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteInvoiceService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public DeleteInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public void execute(Long invoiceId) {
        log.info("Deleting invoice", kv("invoiceId", invoiceId));
        if (!invoiceRepositoryPort.existsById(invoiceId)) {
            log.warn("Invoice not found for deletion", kv("invoiceId", invoiceId));
            throw new EntityNotFoundException("Factura no encontrada");
        }
        invoiceRepositoryPort.deleteById(invoiceId);
        log.info("Invoice deleted", kv("invoiceId", invoiceId));
    }
}
