package com.billMate.billing.application.useCase;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.CreateClientCommand;
import com.billMate.billing.domain.client.port.out.ClientRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CreateClientServiceTest {

    private FakeClientRepository fakeRepository;
    private CreateClientService createClientService;

    @BeforeEach
    void setUp() {
        fakeRepository = new FakeClientRepository();
        createClientService = new CreateClientService(fakeRepository);
    }

    @Test
    @DisplayName("Should create client and delegate to repository port")
    void shouldCreateClientAndSave() {
        CreateClientCommand command = new CreateClientCommand(
                "Acme Corp", "contact@acme.com", "+34 600 123 456", "B12345678", "Calle Mayor 1"
        );

        Client result = createClientService.execute(command);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Acme Corp", result.getName());
        assertEquals("contact@acme.com", result.getEmail());
        assertEquals("+34 600 123 456", result.getPhone());
        assertEquals("B12345678", result.getNif());
        assertEquals("Calle Mayor 1", result.getAddress());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    @DisplayName("Should pass client with null id to repository before save")
    void shouldPassClientWithNullIdBeforeSave() {
        CreateClientCommand command = new CreateClientCommand(
                "Test SL", "test@test.com", null, "A00000000", null
        );

        createClientService.execute(command);

        assertEquals(1, fakeRepository.getSavedClients().size());
        Client saved = fakeRepository.getSavedClients().get(0);
        assertEquals(1L, saved.getId());
        assertEquals("Test SL", saved.getName());
        assertEquals("test@test.com", saved.getEmail());
        assertNull(saved.getPhone());
        assertEquals("A00000000", saved.getNif());
        assertNull(saved.getAddress());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("Should call repository save exactly once")
    void shouldCallSaveOnce() {
        CreateClientCommand command = new CreateClientCommand(
                "Empresa SA", "info@empresa.com", "+34 911 222 333", "C99999999", "Av. Principal 10"
        );

        createClientService.execute(command);

        assertEquals(1, fakeRepository.getSaveCount());
    }

    @Test
    @DisplayName("Should throw when command has invalid data")
    void shouldThrowWhenCommandIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> new CreateClientCommand(null, "a@b.com", null, "X1234", null));

        assertThrows(IllegalArgumentException.class,
                () -> new CreateClientCommand("Name", null, null, "X1234", null));

        assertThrows(IllegalArgumentException.class,
                () -> new CreateClientCommand("Name", "a@b.com", null, null, null));
    }

    // Fake in-memory implementation — no frameworks, no DB
    private static class FakeClientRepository implements ClientRepositoryPort {

        private final List<Client> savedClients = new ArrayList<>();
        private long idSequence = 1;
        private int saveCount = 0;

        @Override
        public Client save(Client client) {
            saveCount++;
            client.setId(idSequence++);
            savedClients.add(client);
            return client;
        }

        @Override
        public Optional<Client> findById(Long id) {
            return savedClients.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public List<Client> findAll() {
            return List.copyOf(savedClients);
        }

        @Override
        public void deleteById(Long id) {
            savedClients.removeIf(c -> c.getId().equals(id));
        }

        @Override
        public boolean existsById(Long id) {
            return savedClients.stream().anyMatch(c -> c.getId().equals(id));
        }

        public List<Client> getSavedClients() {
            return savedClients;
        }

        public int getSaveCount() {
            return saveCount;
        }
    }
}
