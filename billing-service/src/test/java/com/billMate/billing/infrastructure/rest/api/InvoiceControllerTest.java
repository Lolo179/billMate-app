package com.billMate.billing.infrastructure.rest.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.port.in.*;
import com.billMate.billing.domain.invoice.model.InvoiceStatus;
import com.billMate.billing.infrastructure.rest.dto.InvoiceDTO;
import com.billMate.billing.infrastructure.rest.dto.NewInvoiceDTO;
import com.billMate.billing.infrastructure.rest.mapper.InvoiceRestMapper;

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

    @MockBean
    private InvoiceRestMapper invoiceRestMapper;

    @Test
    void givenExistingInvoiceId_whenGetInvoice_thenReturnsInvoice() throws Exception {
        Invoice mock = new Invoice(1L, 2L, List.of(), null, InvoiceStatus.SENT, null,
                BigDecimal.valueOf(100.00), BigDecimal.valueOf(21), null);

        when(getInvoiceUseCase.execute(1L)).thenReturn(mock);

        InvoiceDTO mockDto = new InvoiceDTO();
        mockDto.setInvoiceId(1L);
        mockDto.setClientId(2L);
        mockDto.setTotal(BigDecimal.valueOf(100.00));
        mockDto.setInvoiceLines(List.of());
        when(invoiceRestMapper.toDto(mock)).thenReturn(mockDto);

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

        when(invoiceRestMapper.toCreateCommand(any(NewInvoiceDTO.class)))
                .thenReturn(new CreateInvoiceCommand(2L, null, null, "DRAFT",
                        List.of(new CreateInvoiceCommand.LineCommand("Servicio de diseño web", 2.0, 150.0))));
        when(createInvoiceUseCase.execute(any(CreateInvoiceCommand.class))).thenReturn(saved);

        InvoiceDTO savedDto = new InvoiceDTO();
        savedDto.setInvoiceId(10L);
        savedDto.setStatus(InvoiceDTO.StatusEnum.DRAFT);
        savedDto.setInvoiceLines(List.of());
        when(invoiceRestMapper.toDto(saved)).thenReturn(savedDto);

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

        when(invoiceRestMapper.toUpdateCommand(eq(1L), any(NewInvoiceDTO.class)))
                .thenReturn(new UpdateInvoiceCommand(1L, 2L, null, null, "PAID", List.of()));
        when(updateInvoiceUseCase.execute(any(UpdateInvoiceCommand.class))).thenReturn(updated);

        InvoiceDTO updatedDto = new InvoiceDTO();
        updatedDto.setInvoiceId(1L);
        updatedDto.setStatus(InvoiceDTO.StatusEnum.PAID);
        updatedDto.setInvoiceLines(List.of());
        when(invoiceRestMapper.toDto(updated)).thenReturn(updatedDto);

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

        InvoiceDTO dto1 = new InvoiceDTO();
        dto1.setInvoiceId(1L);
        dto1.setInvoiceLines(List.of());
        InvoiceDTO dto2 = new InvoiceDTO();
        dto2.setInvoiceId(2L);
        dto2.setInvoiceLines(List.of());
        when(invoiceRestMapper.toDto(i1)).thenReturn(dto1);
        when(invoiceRestMapper.toDto(i2)).thenReturn(dto2);

        mockMvc.perform(get("/invoices/client/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].invoiceId").value(1))
                .andExpect(jsonPath("$[1].invoiceId").value(2));
    }
}
