package com.billMate.billing.service;

import com.billMate.billing.model.ClientDTO;
import com.billMate.billing.model.NewClientDTO;

import java.util.List;

public interface ClientService {

    ClientDTO createClient(NewClientDTO newClientDTO);

    ClientDTO getClientById(Long clientId);

    List<ClientDTO> getAllClients();

    ClientDTO updateClient(Long clientId, NewClientDTO newClientDTO);

    void deleteClient(Long clientId);
}
