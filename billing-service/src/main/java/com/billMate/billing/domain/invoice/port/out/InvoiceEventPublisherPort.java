package com.billMate.billing.domain.invoice.port.out;

import com.billMate.billing.domain.invoice.event.InvoiceCreatedEvent;

public interface InvoiceEventPublisherPort {

    void publishInvoiceCreated(InvoiceCreatedEvent event);
}
