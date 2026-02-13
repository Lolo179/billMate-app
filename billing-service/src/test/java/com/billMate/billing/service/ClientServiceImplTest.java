package com.billMate.billing.service;

import com.billMate.billing.entity.ClientEntity;
import com.billMate.billing.model.ClientDTO;
import com.billMate.billing.model.NewClientDTO;
import com.billMate.billing.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ClientServiceImplTest {

    private ClientRepository clientRepository;
    private ModelMapper modelMapper;
    private ClientServiceImpl clientService;

    @BeforeEach
    void setUp() {
        clientRepository = mock(ClientRepository.class);
        modelMapper = new ModelMapper();
        clientService = new ClientServiceImpl(clientRepository, modelMapper);
    }

    @Test
    void createClient_shouldReturnSavedClient() {
        // Given
        NewClientDTO inputDTO = new NewClientDTO()
                .name("Juan")
                .email("juan@mail.com")
                .nif("12345678Z")
                .address("Calle Falsa 123");

        ClientEntity savedEntity = ClientEntity.builder()
                .id(1L)
                .name("Juan")
                .email("juan@mail.com")
                .nif("12345678Z")
                .address("Calle Falsa 123")
                .createdAt(OffsetDateTime.now())
                .invoices(new ArrayList<>())
                .build();

        when(clientRepository.save(any(ClientEntity.class))).thenReturn(savedEntity);

        // When
        ClientDTO result = clientService.createClient(inputDTO);

        // Then
        assertNotNull(result);
        assertEquals("Juan", result.getName());
        assertEquals("juan@mail.com", result.getEmail());
        verify(clientRepository, times(1)).save(any());
    }

    @Test
    void getClientById_shouldReturnClient_whenExists() {
        // Given
        ClientEntity entity = ClientEntity.builder()
                .id(1L)
                .name("Ana")
                .email("ana@mail.com")
                .nif("98765432X")
                .address("Calle Luna 456")
                .createdAt(OffsetDateTime.now())
                .invoices(new ArrayList<>())
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));

        // When
        ClientDTO result = clientService.getClientById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Ana", result.getName());
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    void getClientById_shouldThrowException_whenNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> clientService.getClientById(99L));
        verify(clientRepository, times(1)).findById(99L);
    }

    @Test
    void getAllClients_shouldReturnMappedList() {
        List<ClientEntity> entities = List.of(
                ClientEntity.builder()
                        .id(1L).name("Ana").email("ana@mail.com").phone("123").nif("NIF1")
                        .address("Dir 1").createdAt(OffsetDateTime.now()).invoices(new ArrayList<>()).build(),
                ClientEntity.builder()
                        .id(2L).name("Luis").email("luis@mail.com").phone("456").nif("NIF2")
                        .address("Dir 2").createdAt(OffsetDateTime.now()).invoices(new ArrayList<>()).build()
        );
        when(clientRepository.findAll()).thenReturn(entities);

        List<ClientDTO> result = clientService.getAllClients();

        assertEquals(2, result.size());
        assertEquals("Ana", result.get(0).getName());
        assertEquals("Luis", result.get(1).getName());
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void updateClient_shouldUpdate_whenClientExists() {
        ClientEntity existing = ClientEntity.builder()
                .id(1L).name("Old").email("old@mail.com").phone("123")
                .nif("NIF").address("Dir").createdAt(OffsetDateTime.now()).invoices(new ArrayList<>()).build();

        NewClientDTO dto = new NewClientDTO()
                .name("New").email("new@mail.com").nif("NIF").address("Nueva");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(clientRepository.save(any(ClientEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ClientDTO result = clientService.updateClient(1L, dto);

        assertEquals("New", result.getName());
        assertEquals("new@mail.com", result.getEmail());
        verify(clientRepository).save(existing);
    }

    @Test
    void updateClient_shouldThrow_whenClientNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());
        NewClientDTO dto = new NewClientDTO().name("X").email("x@mail.com");

        assertThrows(RuntimeException.class, () -> clientService.updateClient(99L, dto));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void deleteClient_shouldDelete_whenClientExists() {
        when(clientRepository.existsById(1L)).thenReturn(true);

        clientService.deleteClient(1L);

        verify(clientRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteClient_shouldThrow_whenClientNotFound() {
        when(clientRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> clientService.deleteClient(99L));
        verify(clientRepository, never()).deleteById(anyLong());
    }
}
