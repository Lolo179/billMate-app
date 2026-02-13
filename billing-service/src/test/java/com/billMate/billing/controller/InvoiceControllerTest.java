package com.billMate.billing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.billMate.billing.model.InvoiceDTO;
import com.billMate.billing.model.NewInvoiceDTO;
import com.billMate.billing.service.InvoiceService;

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
    private InvoiceService invoiceService;

    @Test
    void givenExistingInvoiceId_whenGetInvoice_thenReturnsInvoice() throws Exception {
        InvoiceDTO mock = new InvoiceDTO()
                .invoiceId(1L)
                .clientId(2L)
                .total(BigDecimal.valueOf(100.00))
                .status(InvoiceDTO.StatusEnum.SENT);

        when(invoiceService.getInvoiceById(1L)).thenReturn(mock);

        mockMvc.perform(get("/invoices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceId").value(1))
                .andExpect(jsonPath("$.clientId").value(2))
                .andExpect(jsonPath("$.total").value(100.00));
    }

    @Test
    void givenValidInvoice_whenPost_thenReturns201() throws Exception {
        NewInvoiceDTO input = new NewInvoiceDTO()
                .clientId(2L)
                .status(NewInvoiceDTO.StatusEnum.DRAFT)
                .invoiceLines(List.of());

        InvoiceDTO saved = new InvoiceDTO()
                .invoiceId(10L)
                .clientId(2L)
                .status(InvoiceDTO.StatusEnum.DRAFT)
                .total(BigDecimal.valueOf(0.0));

        when(invoiceService.createInvoice(any())).thenReturn(saved);

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
        InvoiceDTO updated = new InvoiceDTO()
                .invoiceId(1L)
                .status(InvoiceDTO.StatusEnum.PAID);

        when(invoiceService.updateInvoice(eq(1L), any())).thenReturn(updated);

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
        doNothing().when(invoiceService).deleteInvoice(1L);

        mockMvc.perform(delete("/invoices/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenClientId_whenGetInvoicesByClient_thenReturnsList() throws Exception {
        InvoiceDTO i1 = new InvoiceDTO().invoiceId(1L).clientId(5L).total(BigDecimal.valueOf(50.0));
        InvoiceDTO i2 = new InvoiceDTO().invoiceId(2L).clientId(5L).total(BigDecimal.valueOf(75.0));

        when(invoiceService.getInvoicesByClientId(5L)).thenReturn(List.of(i1, i2));

        mockMvc.perform(get("/invoices/client/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].invoiceId").value(1))
                .andExpect(jsonPath("$[1].invoiceId").value(2));
    }
}
