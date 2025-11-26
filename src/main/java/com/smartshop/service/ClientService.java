package com.smartshop.service;

import com.smartshop.dto.ClientStatistics;
import com.smartshop.dto.UserDTO;

import java.util.List;

public interface ClientService {

    UserDTO createClient(UserDTO userDTO);

    UserDTO getClientById(Long id);

    List<UserDTO> getAllClients();

    UserDTO updateClient(Long id, UserDTO userDTO);

    void deleteClient(Long id);

    ClientStatistics getClientStatistics(Long clientId);
}
