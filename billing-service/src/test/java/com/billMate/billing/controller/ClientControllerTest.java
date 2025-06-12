package com.billMate.billing.controller;

import com.billMate.billing.TestBillingAplication;
import com.billMate.billing.exception.ErrorMessages;
import com.billMate.billing.exception.GlobalExceptionHandler;
import com.billMate.billing.model.ClientDTO;
import com.billMate.billing.model.NewClientDTO;
import com.billMate.billing.service.ClientService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClientController.class)
@Import(GlobalExceptionHandler.class)
@ContextConfiguration(classes = TestBillingAplication.class)
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void givenExistingClientId_whenGetClientById_thenReturnClientAndStatus200() throws Exception {

        //Given
        ClientDTO mockClient = new ClientDTO("Luis Alvarado", "luis@gmail.com", "51246869s", "calle falsa 123", 1L);
        when(clientService.getClientById(1L)).thenReturn(mockClient);

        //When & then
        mockMvc.perform(get("/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luis Alvarado"))
                .andExpect(jsonPath("$.email").value("luis@gmail.com"))
                .andExpect(jsonPath("$.nif").value("51246869s"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void givenInvalidClientId_whenGetClientById_thenReturns404WithJson() throws Exception {

        // Given
        when(clientService.getClientById(999L))
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void givenValidClient_whenPostClient_thenReturns201() throws Exception {

        // Given
        NewClientDTO input = new NewClientDTO("Ana Torres", "ana@example.com", "12345678X", "Calle Real 123");
        ClientDTO created = new ClientDTO("Ana Torres", "ana@example.com", "12345678X", "Calle Real 123", 10L);

        when(clientService.createClient(any(NewClientDTO.class))).thenReturn(created);

        // When & Then
        mockMvc.perform(post("/clients")
                        .with(csrf())
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void givenValidUpdate_whenPutClient_thenReturns200AndUpdatedClient() throws Exception {
        // Given
        Long clientId = 1L;
        NewClientDTO updateDTO = new NewClientDTO("Luis Modificado", "luis_mod@example.com", "87654321Z", "Nueva Calle 456");
        ClientDTO updatedClient = new ClientDTO("Luis Modificado", "luis_mod@example.com", "87654321Z", "Nueva Calle 456", clientId);

        when(clientService.updateClient(eq(clientId), any(NewClientDTO.class))).thenReturn(updatedClient);

        // When & Then
        mockMvc.perform(put("/clients/{clientId}", clientId)
                        .with(csrf())
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void givenNonexistentClientId_whenPutClient_thenReturns404() throws Exception {
        // Given
        Long clientId = 999L;
        when(clientService.updateClient(eq(clientId), any(NewClientDTO.class)))
                .thenThrow(new EntityNotFoundException("ID 999 no encontrado"));

        // When & Then
        mockMvc.perform(put("/clients/{clientId}", clientId)
                        .with(csrf())
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void givenExistingClientId_whenDeleteClient_thenReturns204() throws Exception {
        // Given
        Long clientId = 1L;

        // No hace falta stub si el método es void
        doNothing().when(clientService).deleteClient(clientId);

        // When & Then
        mockMvc.perform(delete("/clients/{id}", clientId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void givenNonexistentClientId_whenDeleteClient_thenReturns404() throws Exception {
        // Given
        Long clientId = 999L;
        doThrow(new EntityNotFoundException("ID 999 no encontrado"))
                .when(clientService).deleteClient(clientId);

        // When & Then
        mockMvc.perform(delete("/clients/{id}", clientId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorMessages.RESOURCE_NOT_FOUND))
                .andExpect(jsonPath("$.errors[0]").value("ID 999 no encontrado"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void givenInvalidClient_whenPostClient_thenReturns400() throws Exception {
        mockMvc.perform(post("/clients")
                        .with(csrf())
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
