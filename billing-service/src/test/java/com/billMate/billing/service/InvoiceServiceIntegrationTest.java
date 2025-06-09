package com.billMate.billing.service;

import com.billMate.billing.BillServiceApplication;
import com.billMate.billing.model.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BillServiceApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.yaml")
@Transactional
public class InvoiceServiceIntegrationTest {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private ClientService clientService;




    @Test
    void givenValidInvoice_whenCreateAndFind_thenPersisted() {
        // Given - primero creamos un cliente válido
        NewClientDTO client = new NewClientDTO("Cliente Test", "test@mail.com", "12345678Z", "Calle Test 123");
        ClientDTO savedClient = clientService.createClient(client);

        NewInvoiceDTO invoice = new NewInvoiceDTO()
                .clientId(savedClient.getClientId())
                .date(LocalDate.now())
                .status(NewInvoiceDTO.StatusEnum.DRAFT)
                .invoiceLines(List.of(new InvoiceLine()
                        .description("Servicio de prueba")
                        .quantity(2.0)
                        .unitPrice(100.0)));

        // When
        InvoiceDTO created = invoiceService.createInvoice(invoice);
        InvoiceDTO found = invoiceService.getInvoiceById(created.getInvoiceId());

        // Then
        assertNotNull(found);
        assertEquals(savedClient.getClientId(), found.getClientId());
        assertEquals(BigDecimal.valueOf(242.00).setScale(2), found.getTotal());
        assertEquals(1, found.getInvoiceLines().size());
    }


    @Test
    void givenExistingInvoice_whenUpdate_thenUpdatedCorrectly() {
        // Given
        ClientDTO client = clientService.createClient(new NewClientDTO("Modificable", "mod@mail.com", "11223344A", "Calle Cambio"));

        NewInvoiceDTO original = new NewInvoiceDTO()
                .clientId(client.getClientId())
                .date(LocalDate.now())
                .invoiceLines(List.of(new InvoiceLine()
                        .description("Servicio")
                        .quantity(1.0)
                        .unitPrice(100.0)));

        InvoiceDTO created = invoiceService.createInvoice(original);

        NewInvoiceDTO updated = new NewInvoiceDTO()
                .clientId(client.getClientId())
                .date(LocalDate.now())
                .status(NewInvoiceDTO.StatusEnum.SENT)
                .description("Actualizada")
                .invoiceLines(List.of(new InvoiceLine()
                        .description("Nuevo servicio")
                        .quantity(3.0)
                        .unitPrice(50.0)));

        // When
        InvoiceDTO result = invoiceService.updateInvoice(created.getInvoiceId(), updated);

        // Then
        assertEquals("Actualizada", result.getDescription());
        assertEquals(BigDecimal.valueOf(150.00).setScale(2), result.getTotal());
        assertEquals("SENT", result.getStatus().name());
    }

    @Test
    void givenInvoice_whenDelete_thenShouldThrowOnGet() {
        // Given
        ClientDTO client = clientService.createClient(new NewClientDTO("Eliminar", "elim@mail.com", "55555555B", "Borrar 123"));

        NewInvoiceDTO dto = new NewInvoiceDTO()
                .clientId(client.getClientId())
                .date(LocalDate.now())
                .invoiceLines(List.of(new InvoiceLine()
                        .description("Temporal")
                        .quantity(1.0)
                        .unitPrice(99.99)));

        InvoiceDTO invoice = invoiceService.createInvoice(dto);
        Long id = invoice.getInvoiceId();

        // When
        invoiceService.deleteInvoice(id);

        // Then
        assertThrows(RuntimeException.class, () -> invoiceService.getInvoiceById(id));
    }

    @Test
    void givenClientWithInvoices_whenGetByClientId_thenReturnList() {
        // Given
        ClientDTO client = clientService.createClient(new NewClientDTO("Con Facturas", "cf@mail.com", "99999999R", "Factura 456"));

        for (int i = 0; i < 2; i++) {
            NewInvoiceDTO dto = new NewInvoiceDTO()
                    .clientId(client.getClientId())
                    .date(LocalDate.now())
                    .invoiceLines(List.of(new InvoiceLine()
                            .description("Item " + i)
                            .quantity(1.0)
                            .unitPrice(100.0)));

            invoiceService.createInvoice(dto);
        }

        // When
        List<InvoiceDTO> invoices = invoiceService.getInvoicesByClientId(client.getClientId());

        // Then
        assertEquals(2, invoices.size());
    }

    @Test
    void givenInvoiceWithoutLines_whenCreate_thenThrowsException() {
        // Given
        ClientDTO client = clientService.createClient(new NewClientDTO("Sin Líneas", "nolines@mail.com", "11112222Z", "Sin calle"));

        NewInvoiceDTO invoice = new NewInvoiceDTO()
                .clientId(client.getClientId())
                .date(LocalDate.now())
                .invoiceLines(Collections.emptyList()); // sin líneas

        // When + Then
        assertThrows(IllegalArgumentException.class, () -> invoiceService.createInvoice(invoice));
    }

    @Test
    void givenLineWithZeroQuantity_whenCreate_thenThrowsException() {
        // Given
        ClientDTO client = clientService.createClient(new NewClientDTO("Cantidad Cero", "zero@mail.com", "22223333Z", "Calle Cero"));

        NewInvoiceDTO invoice = new NewInvoiceDTO()
                .clientId(client.getClientId())
                .date(LocalDate.now())
                .invoiceLines(List.of(
                        new InvoiceLine()
                                .description("Producto inválido")
                                .quantity(0.0)
                                .unitPrice(100.0)
                ));

        // When + Then
        assertThrows(IllegalArgumentException.class, () -> invoiceService.createInvoice(invoice));
    }

    @Test
    void givenNonexistentClient_whenCreateInvoice_thenThrowsException() {
        // Given
        NewInvoiceDTO invoice = new NewInvoiceDTO()
                .clientId(99999L) // cliente inexistente
                .date(LocalDate.now())
                .invoiceLines(List.of(new InvoiceLine()
                        .description("Inexistente")
                        .quantity(1.0)
                        .unitPrice(100.0)));

        // When + Then
        assertThrows(EntityNotFoundException.class, () -> invoiceService.createInvoice(invoice));
    }

    @Test
    void givenLineWithoutDescription_whenCreate_thenThrowsException() {
        ClientDTO client = clientService.createClient(new NewClientDTO("Sin Desc", "desc@mail.com", "33334444Z", "Calle sin nombre"));

        NewInvoiceDTO invoice = new NewInvoiceDTO()
                .clientId(client.getClientId())
                .date(LocalDate.now())
                .invoiceLines(List.of(
                        new InvoiceLine()
                                .description("") // vacío
                                .quantity(1.0)
                                .unitPrice(100.0)
                ));

        assertThrows(IllegalArgumentException.class, () -> invoiceService.createInvoice(invoice));
    }

    @Test
    void givenInvoice_whenEmit_thenStatusChangesToSent() {
        // Given
        ClientDTO client = clientService.createClient(
                new NewClientDTO("Cliente Emitir", "emit@mail.com", "11112222B", "Calle Emisión"));

        NewInvoiceDTO dto = new NewInvoiceDTO()
                .clientId(client.getClientId())
                .date(LocalDate.now())
                .status(NewInvoiceDTO.StatusEnum.DRAFT)
                .invoiceLines(List.of(new InvoiceLine()
                        .description("Servicio")
                        .quantity(1.0)
                        .unitPrice(50.0)));

        InvoiceDTO created = invoiceService.createInvoice(dto);

        // When
        byte[] pdfBytes = invoiceService.emitInvoice(created.getInvoiceId());

        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "El PDF generado no debe estar vacío");

        InvoiceDTO updated = invoiceService.getInvoiceById(created.getInvoiceId());
        assertEquals("SENT", updated.getStatus().name());
    }

    @Test
    void givenAlreadySentInvoice_whenEmitAgain_thenThrowsException() {
        // Given
        ClientDTO client = clientService.createClient(
                new NewClientDTO("Cliente Emitido", "sent@mail.com", "12344321Z", "Calle Sent"));

        NewInvoiceDTO dto = new NewInvoiceDTO()
                .clientId(client.getClientId())
                .date(LocalDate.now())
                .status(NewInvoiceDTO.StatusEnum.DRAFT)
                .invoiceLines(List.of(new InvoiceLine()
                        .description("Servicio único")
                        .quantity(1.0)
                        .unitPrice(100.0)));

        InvoiceDTO created = invoiceService.createInvoice(dto);

        // Emitir una vez (válido)
        byte[] firstPdf = invoiceService.emitInvoice(created.getInvoiceId());
        assertNotNull(firstPdf);

        // When - intentar emitir de nuevo
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> invoiceService.emitInvoice(created.getInvoiceId())
        );

        // Then
        assertEquals("Solo las facturas en estado DRAFT pueden ser emitidas.", exception.getMessage());
    }


    @Test
    void emitInvoice_shouldFail_whenInvoiceDoesNotExist() {
        // Given
        Long fakeId = 9999L;

        // When & Then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            invoiceService.emitInvoice(fakeId);
        });
        assertEquals("Factura no encontrada", ex.getMessage());
    }


}
