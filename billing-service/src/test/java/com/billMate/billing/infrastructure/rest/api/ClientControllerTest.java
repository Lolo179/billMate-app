package com.billMate.billing.infrastructure.rest.api;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.client.port.in.*;
import com.billMate.billing.infrastructure.rest.error.ErrorMessages;
import com.billMate.billing.infrastructure.rest.dto.ClientDTO;
import com.billMate.billing.infrastructure.rest.dto.NewClientDTO;
import com.billMate.billing.infrastructure.rest.mapper.ClientRestMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClientController.class)
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateClientUseCase createClientUseCase;

    @MockBean
    private GetClientUseCase getClientUseCase;

    @MockBean
    private GetAllClientsUseCase getAllClientsUseCase;

    @MockBean
    private UpdateClientUseCase updateClientUseCase;

    @MockBean
    private DeleteClientUseCase deleteClientUseCase;

    @MockBean
    private ClientRestMapper clientRestMapper;

    @Test
    void givenExistingClientId_whenGetClientById_thenReturnClientAndStatus200() throws Exception {

        //Given
        Client mockClient = new Client(1L, "Luis Alvarado", "luis@gmail.com", null, "51246869s", "calle falsa 123", OffsetDateTime.now());
        when(getClientUseCase.execute(1L)).thenReturn(mockClient);

        ClientDTO mockDto = new ClientDTO();
        mockDto.setClientId(1L);
        mockDto.setName("Luis Alvarado");
        mockDto.setEmail("luis@gmail.com");
        mockDto.setNif("51246869s");
        when(clientRestMapper.toDto(mockClient)).thenReturn(mockDto);

        //When & then
        mockMvc.perform(get("/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luis Alvarado"))
                .andExpect(jsonPath("$.email").value("luis@gmail.com"))
                .andExpect(jsonPath("$.nif").value("51246869s"));
    }

    @Test
    void givenInvalidClientId_whenGetClientById_thenReturns404WithJson() throws Exception {

        // Given
        when(getClientUseCase.execute(999L))
                .thenThrow(new EntityNotFoundException("ID 999 no encontrado"));

        // When & Then
        mockMvc.perform(get("/clients/999"))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(ErrorMessages.RESOURCE_NOT_FOUND))
                .andExpect(jsonPath("$.errors[0]").value("ID 999 no encontrado"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void givenValidClient_whenPostClient_thenReturns201() throws Exception {

        // Given
        Client createdClient = new Client(10L, "Ana Torres", "ana@example.com", null, "12345678X", "Calle Real 123", OffsetDateTime.now());
        when(clientRestMapper.toCreateCommand(any(NewClientDTO.class)))
                .thenReturn(new CreateClientCommand("Ana Torres", "ana@example.com", null, "12345678X", "Calle Real 123"));
        when(createClientUseCase.execute(any(CreateClientCommand.class))).thenReturn(createdClient);

        ClientDTO createdDto = new ClientDTO();
        createdDto.setClientId(10L);
        createdDto.setName("Ana Torres");
        createdDto.setEmail("ana@example.com");
        when(clientRestMapper.toDto(createdClient)).thenReturn(createdDto);

        // When & Then
        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "name": "Ana Torres",
                    "email": "ana@example.com",
                    "nif": "12345678X",
                    "address": "Calle Real 123"
                }
            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(10))
                .andExpect(jsonPath("$.name").value("Ana Torres"))
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void givenValidUpdate_whenPutClient_thenReturns200AndUpdatedClient() throws Exception {
        // Given
        Long clientId = 1L;
        Client updatedDomain = new Client(clientId, "Luis Modificado", "luis_mod@example.com", null, "87654321Z", "Nueva Calle 456", OffsetDateTime.now());

        when(clientRestMapper.toUpdateCommand(eq(clientId), any(NewClientDTO.class)))
                .thenReturn(new UpdateClientCommand(clientId, "Luis Modificado", "luis_mod@example.com", null, "87654321Z", "Nueva Calle 456"));
        when(updateClientUseCase.execute(any(UpdateClientCommand.class))).thenReturn(updatedDomain);

        ClientDTO updatedDto = new ClientDTO();
        updatedDto.setClientId(clientId);
        updatedDto.setName("Luis Modificado");
        updatedDto.setEmail("luis_mod@example.com");
        updatedDto.setNif("87654321Z");
        updatedDto.setAddress("Nueva Calle 456");
        when(clientRestMapper.toDto(updatedDomain)).thenReturn(updatedDto);

        // When & Then
        mockMvc.perform(put("/clients/{clientId}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "name": "Luis Modificado",
                  "email": "luis_mod@example.com",
                  "nif": "87654321Z",
                  "address": "Nueva Calle 456"
                }
            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.name").value("Luis Modificado"))
                .andExpect(jsonPath("$.email").value("luis_mod@example.com"));
    }

    @Test
    void givenNonexistentClientId_whenPutClient_thenReturns404() throws Exception {
        // Given
        Long clientId = 999L;
        when(clientRestMapper.toUpdateCommand(eq(clientId), any(NewClientDTO.class)))
                .thenReturn(new UpdateClientCommand(clientId, "Nombre", "correo@example.com", null, "11111111A", "Dirección"));
        when(updateClientUseCase.execute(any(UpdateClientCommand.class)))
                .thenThrow(new EntityNotFoundException("ID 999 no encontrado"));

        // When & Then
        mockMvc.perform(put("/clients/{clientId}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "name": "Nombre",
                  "email": "correo@example.com",
                  "nif": "11111111A",
                  "address": "Dirección"
                }
            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorMessages.RESOURCE_NOT_FOUND))
                .andExpect(jsonPath("$.errors[0]").value("ID 999 no encontrado"));
    }

    @Test
    void givenExistingClientId_whenDeleteClient_thenReturns204() throws Exception {
        // Given
        Long clientId = 1L;

        doNothing().when(deleteClientUseCase).execute(clientId);

        // When & Then
        mockMvc.perform(delete("/clients/{clientId}", clientId))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenNonexistentClientId_whenDeleteClient_thenReturns404() throws Exception {
        // Given
        Long clientId = 999L;
        doThrow(new EntityNotFoundException("ID 999 no encontrado"))
                .when(deleteClientUseCase).execute(clientId);

        // When & Then
        mockMvc.perform(delete("/clients/{clientId}", clientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorMessages.RESOURCE_NOT_FOUND))
                .andExpect(jsonPath("$.errors[0]").value("ID 999 no encontrado"));
    }

    @Test
    void givenInvalidClient_whenPostClient_thenReturns400() throws Exception {
        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "name": "",
                    "email": "no-es-un-email",
                    "nif": "INVALIDO",
                    "address": ""
                }
            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(ErrorMessages.VALIDATION_FAILED))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
    }


}
