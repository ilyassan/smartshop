package com.smartshop.service;

import com.smartshop.dto.ClientResponse;
import com.smartshop.dto.CreateClientRequest;
import com.smartshop.dto.UpdateClientRequest;

import java.util.List;

public interface ClientService {

    ClientResponse createClient(CreateClientRequest request);

    ClientResponse getClientById(Long id);

    List<ClientResponse> getAllClients();

    ClientResponse updateClient(Long id, UpdateClientRequest request);

    void deleteClient(Long id);
}
