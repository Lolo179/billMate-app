package com.billMate.billing.infrastructure.rest.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.port.in.*;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;


@WebMvcTest(InvoiceController.class)
public class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateInvoiceUseCase createInvoiceUseCase;

    @MockBean
    private GetInvoiceUseCase getInvoiceUseCase;

    @MockBean
    private GetAllInvoicesUseCase getAllInvoicesUseCase;

    @MockBean
    private GetInvoicesByClientUseCase getInvoicesByClientUseCase;

    @MockBean
    private UpdateInvoiceUseCase updateInvoiceUseCase;

    @MockBean
    private DeleteInvoiceUseCase deleteInvoiceUseCase;

    @MockBean
    private EmitInvoiceUseCase emitInvoiceUseCase;

    @MockBean
    private DownloadInvoicePdfUseCase downloadInvoicePdfUseCase;

    @MockBean
    private PayInvoiceUseCase payInvoiceUseCase;

    @Test
    void givenExistingInvoiceId_whenGetInvoice_thenReturnsInvoice() throws Exception {
        Invoice mock = new Invoice(1L, 2L, List.of(), null, InvoiceStatus.SENT, null,
                BigDecimal.valueOf(100.00), BigDecimal.valueOf(21), null);

        when(getInvoiceUseCase.execute(1L)).thenReturn(mock);

        mockMvc.perform(get("/invoices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceId").value(1))
                .andExpect(jsonPath("$.clientId").value(2))
                .andExpect(jsonPath("$.total").value(100.00));
    }

    @Test
    void givenValidInvoice_whenPost_thenReturns201() throws Exception {
        Invoice saved = new Invoice(10L, 2L, List.of(), null, InvoiceStatus.DRAFT, null,
                BigDecimal.valueOf(0.0), BigDecimal.valueOf(21), null);

        when(createInvoiceUseCase.execute(any(CreateInvoiceCommand.class))).thenReturn(saved);

        mockMvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": 2,
                                  "date": "2025-06-01",
                                  "status": "DRAFT",
                                  "invoiceLines": [
                                  {
                                                "description": "Servicio de diseño web",
                                                "quantity": 2,
                                                "unitPrice": 150.0
                                              }]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceId").value(10))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void givenUpdate_whenPutInvoice_thenReturns200() throws Exception {
        Invoice updated = new Invoice(1L, 2L, List.of(), null, InvoiceStatus.PAID, null,
                BigDecimal.ZERO, BigDecimal.valueOf(21), null);

        when(updateInvoiceUseCase.execute(any(UpdateInvoiceCommand.class))).thenReturn(updated);

        mockMvc.perform(put("/invoices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": 2,
                                  "date": "2025-04-01",
                                  "status": "PAID",
                                  "invoiceLines": [
                                  {
                                                "description": "Servicio de diseño web",
                                                "quantity": 2,
                                                "unitPrice": 150.0
                                              }]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceId").value(1))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void givenInvoiceId_whenDelete_thenReturns204() throws Exception {
        doNothing().when(deleteInvoiceUseCase).execute(1L);

        mockMvc.perform(delete("/invoices/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenClientId_whenGetInvoicesByClient_thenReturnsList() throws Exception {
        Invoice i1 = new Invoice(1L, 5L, List.of(), null, null, null,
                BigDecimal.valueOf(50.0), BigDecimal.valueOf(21), null);
        Invoice i2 = new Invoice(2L, 5L, List.of(), null, null, null,
                BigDecimal.valueOf(75.0), BigDecimal.valueOf(21), null);

        when(getInvoicesByClientUseCase.execute(5L)).thenReturn(List.of(i1, i2));

        mockMvc.perform(get("/invoices/client/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].invoiceId").value(1))
                .andExpect(jsonPath("$[1].invoiceId").value(2));
    }
}
