package com.billMate.billing.service;

import com.billMate.billing.entity.ClientEntity;
import com.billMate.billing.model.Client;
import com.billMate.billing.repository.JpaRepository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;
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
        modelMapper = new ModelMapper(); // usamos real
        clientService = new ClientServiceImpl(clientRepository, modelMapper);
    }

    @Test
    void createClient_shouldReturnSavedClient(){

        //Given
        Client inputDTO = new Client()
                .name("Juan")
                .email("juan@mail.com")
                .nif("12345678Z")
                .address("Calle Falsa 123");

        ClientEntity savedEntity = new ClientEntity();
        savedEntity.setId(1L);
        savedEntity.setName("Juan");
        savedEntity.setEmail("juan@mail.com");
        savedEntity.setNif("12345678Z");
        savedEntity.setAddress("Calle Falsa 123");
        savedEntity.setCreatedAt(OffsetDateTime.now());

        when(clientRepository.save(any(ClientEntity.class))).thenReturn(savedEntity);

        //When
        Client result = clientService.createClient(inputDTO);

        //Then
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
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(entity));

        // When
        Client result = clientService.getClientById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Ana", result.getName());
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    void getClientById_shouldThrowException_whenNotFound() {
        // Given
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(RuntimeException.class, () -> clientService.getClientById(99L));
        verify(clientRepository, times(1)).findById(99L);
    }
}
