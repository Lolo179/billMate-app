package com.billMate.billing.application.service;

import com.billMate.billing.infrastructure.rest.dto.ClientDTO;
import com.billMate.billing.infrastructure.rest.dto.NewClientDTO;

import java.util.List;

public interface ClientService {

    ClientDTO createClient(NewClientDTO newClientDTO);

    ClientDTO getClientById(Long clientId);

    List<ClientDTO> getAllClients();

    ClientDTO updateClient(Long clientId, NewClientDTO newClientDTO);

    void deleteClient(Long clientId);
}
