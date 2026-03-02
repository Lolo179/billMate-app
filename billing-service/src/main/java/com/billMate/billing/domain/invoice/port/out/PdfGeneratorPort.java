package com.billMate.billing.domain.invoice.port.out;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.invoice.model.Invoice;

public interface PdfGeneratorPort {

    byte[] generate(Invoice invoice, Client client);
}
