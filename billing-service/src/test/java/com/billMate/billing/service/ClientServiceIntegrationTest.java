package com.billMate.billing.service;

import com.billMate.billing.BillingIntegrationTestBase;
import com.billMate.billing.model.ClientDTO;
import com.billMate.billing.model.NewClientDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
public class ClientServiceIntegrationTest extends BillingIntegrationTestBase {

    @Autowired
    private ClientService clientService;

    @Test
    void givenValidClient_whenCreateAndFindById_thenClientIsPersisted(){

        // Given
        NewClientDTO input = new NewClientDTO("Carlos Ruiz", "carlos@mail.com", "12345678Z", "Calle Luna 45");

        // When
        ClientDTO saved = clientService.createClient(input);
        ClientDTO found = clientService.getClientById(saved.getClientId());

        // Then
        assertEquals("Carlos Ruiz", found.getName());
        assertEquals("carlos@mail.com", found.getEmail());
        assertEquals("12345678Z", found.getNif());
    }
    @Test
    void givenExistingClient_whenUpdate_thenDataIsUpdated() {
        // Given
        NewClientDTO input = new NewClientDTO("Lucía", "lucia@mail.com", "12345678A", "Calle Sol 9");
        ClientDTO saved = clientService.createClient(input);

        NewClientDTO update = new NewClientDTO("Lucía Gómez", "lucia.gomez@mail.com", "87654321B", "Avenida Mar 21");

        // When
        ClientDTO updated = clientService.updateClient(saved.getClientId(), update);

        // Then
        assertEquals("Lucía Gómez", updated.getName());
        assertEquals("lucia.gomez@mail.com", updated.getEmail());
        assertEquals("87654321B", updated.getNif());
    }

    @Test
    void givenClient_whenDelete_thenShouldThrowOnFind() {
        // Given
        NewClientDTO input = new NewClientDTO("Mario", "mario@mail.com", "23456789C", "Calle Roja 7");
        ClientDTO saved = clientService.createClient(input);
        Long id = saved.getClientId();

        // When
        clientService.deleteClient(id);

        // Then
        assertThrows(EntityNotFoundException.class, () -> clientService.getClientById(id));
    }
}
