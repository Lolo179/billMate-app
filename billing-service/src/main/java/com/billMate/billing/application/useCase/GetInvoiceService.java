package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.GetInvoiceUseCase;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class GetInvoiceService implements GetInvoiceUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetInvoiceService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;

    public GetInvoiceService(InvoiceRepositoryPort invoiceRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
    }

    @Override
    public Invoice execute(Long invoiceId) {
        log.debug("Finding invoice", kv("invoiceId", invoiceId));
        return invoiceRepositoryPort.findById(invoiceId)
                .orElseThrow(() -> {
                    log.warn("Invoice not found", kv("invoiceId", invoiceId));
                    return new EntityNotFoundException("Factura no encontrada");
                });
    }
}
