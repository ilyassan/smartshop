package com.smartshop.service;

import com.smartshop.entity.User;

import java.util.List;

public interface ClientService {

    User createClient(User user);

    User getClientById(Long id);

    List<User> getAllClients();

    User updateClient(Long id, User user);

    void deleteClient(Long id);
}
