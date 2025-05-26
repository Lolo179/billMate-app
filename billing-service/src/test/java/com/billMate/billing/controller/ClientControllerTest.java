package com.billMate.billing.controller;

import com.billMate.billing.TestBillingAplication;
import com.billMate.billing.config.MapperConfig;
import com.billMate.billing.config.SecurityConfig;
import com.billMate.billing.exception.ErrorMessages;
import com.billMate.billing.exception.GlobalExceptionHandler;
import com.billMate.billing.model.ClientDTO;
import com.billMate.billing.repository.ClientRepository;
import com.billMate.billing.service.ClientService;
import com.billMate.billing.service.ClientServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = ClientController.class)
@Import(GlobalExceptionHandler.class)
@ContextConfiguration(classes = TestBillingAplication.class)
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer dummy-token";


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
                .andExpect(jsonPath("$.message").value(ErrorMessages.CLIENT_NOT_FOUND))
                .andExpect(jsonPath("$.errors[0]").value("ID 999 no encontrado"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
