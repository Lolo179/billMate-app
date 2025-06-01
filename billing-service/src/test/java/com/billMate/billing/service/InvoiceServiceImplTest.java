package com.billMate.billing.service;

import com.billMate.billing.entity.ClientEntity;
import com.billMate.billing.entity.InvoiceEntity;
import com.billMate.billing.enums.InvoiceStatus;
import com.billMate.billing.model.InvoiceDTO;

import com.billMate.billing.model.InvoiceLine;
import com.billMate.billing.model.NewInvoiceDTO;
import com.billMate.billing.repository.ClientRepository;
import com.billMate.billing.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InvoiceServiceImplTest {

    private InvoiceRepository invoiceRepository;
    private ClientRepository clientRepository;
    private ModelMapper modelMapper;
    private InvoiceServiceImpl invoiceService;

    @BeforeEach
    void setUp() {
        invoiceRepository = mock(InvoiceRepository.class);
        clientRepository = mock(ClientRepository.class);
        modelMapper = new ModelMapper();
        invoiceService = new InvoiceServiceImpl(invoiceRepository, clientRepository, modelMapper);
    }

    @Test
    void getInvoiceById_shouldReturnInvoice_whenExists() {
        // Given
        InvoiceEntity entity = InvoiceEntity.builder()
                .invoiceId(10L)
                .client(new ClientEntity(1L, "Cliente", "cliente@mail.com", "600", "12345678Z", "Dirección", OffsetDateTime.now()))
                .date(LocalDate.now())
                .status(InvoiceStatus.PAID)
                .createdAt(LocalDateTime.now())
                .invoiceLines(List.of())
                .total(BigDecimal.valueOf(100))
                .build();

        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(entity));

        // When
        InvoiceDTO result = invoiceService.getInvoiceById(10L);

        // Then
        assertNotNull(result);
        assertEquals(Long.valueOf(10L), result.getInvoiceId());
        verify(invoiceRepository, times(1)).findById(10L);
    }

    @Test
    void updateInvoice_shouldUpdate_whenExists() {
        // Given
        ClientEntity mockClient = ClientEntity.builder()
                .id(1L)
                .name("Mock Cliente")
                .email("mock@mail.com")
                .nif("12345678A")
                .phone("123456789")
                .address("Calle Falsa 123")
                .createdAt(OffsetDateTime.now())
                .build();

        InvoiceEntity existing = InvoiceEntity.builder()
                .invoiceId(10L)
                .client(mockClient)
                .invoiceLines(new ArrayList<>())
                .build();

        NewInvoiceDTO dto = new NewInvoiceDTO()
                .clientId(1L)
                .date(LocalDate.now())
                .status(NewInvoiceDTO.StatusEnum.SENT)
                .description("Actualizada")
                .invoiceLines(List.of(
                        new InvoiceLine()
                                .description("Servicio actualizado")
                                .quantity(2.0)
                                .unitPrice(100.0)
                ));

        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(mockClient));
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        InvoiceDTO result = invoiceService.updateInvoice(10L, dto);

        // Then
        assertNotNull(result);
        assertEquals("Actualizada", result.getDescription());
        assertEquals(BigDecimal.valueOf(200.00).setScale(2), result.getTotal());
        verify(invoiceRepository).save(existing);
    }

    @Test
    void deleteInvoice_shouldDelete_whenExists() {
        // Given
        when(invoiceRepository.existsById(10L)).thenReturn(true);

        // When
        invoiceService.deleteInvoice(10L);

        // Then
        verify(invoiceRepository).deleteById(10L);
    }

    @Test
    void deleteInvoice_shouldThrow_whenNotFound() {
        // Given
        when(invoiceRepository.existsById(99L)).thenReturn(false);

        // When + Then
        assertThrows(RuntimeException.class, () -> invoiceService.deleteInvoice(99L));
        verify(invoiceRepository, never()).deleteById(anyLong());
    }

    @Test
    void getInvoicesByClientId_shouldReturnList() {
        // Given
        List<InvoiceEntity> invoices = List.of(
                InvoiceEntity.builder().invoiceId(1L).invoiceLines(List.of()).build(),
                InvoiceEntity.builder().invoiceId(2L).invoiceLines(List.of()).build()
        );

        when(invoiceRepository.findAllByClient_Id(1L)).thenReturn(invoices);

        // When
        List<InvoiceDTO> result = invoiceService.getInvoicesByClientId(1L);

        // Then
        assertEquals(2, result.size());
        verify(invoiceRepository).findAllByClient_Id(1L);
    }
}
