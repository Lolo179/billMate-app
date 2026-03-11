package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.GetInvoicesByClientUseCase;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import com.billMate.billing.domain.invoice.port.out.InvoiceRepositoryPort;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class GetInvoicesByClientService implements GetInvoicesByClientUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetInvoicesByClientService.class);
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;

    public GetInvoicesByClientService(InvoiceRepositoryPort invoiceRepositoryPort,
                                     ClientRepositoryPort clientRepositoryPort) {
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public List<Invoice> execute(Long clientId) {
        log.debug("Fetching invoices by client", kv("clientId", clientId));
        clientRepositoryPort.findById(clientId)
                .orElseThrow(() -> {
                    log.warn("Client not found", kv("clientId", clientId));
                    return new EntityNotFoundException(
                        "Cliente con ID " + clientId + " no encontrado.");
                });
        List<Invoice> invoices = invoiceRepositoryPort.findAllByClientId(clientId);
        log.debug("Invoices found for client", kv("clientId", clientId), kv("count", invoices.size()));
        return invoices;
    }
}
