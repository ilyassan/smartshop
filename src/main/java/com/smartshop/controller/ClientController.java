package com.smartshop.controller;

import com.smartshop.entity.User;
import com.smartshop.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<User> createClient(@RequestBody User user) {
        log.info("Creating new client with username: {}", user.getUsername());
        User createdClient = clientService.createClient(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getClientById(@PathVariable Long id) {
        log.info("Fetching client with id: {}", id);
        User client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllClients() {
        log.info("Fetching all clients");
        List<User> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateClient(@PathVariable Long id, @RequestBody User user) {
        log.info("Updating client with id: {}", id);
        User updatedClient = clientService.updateClient(id, user);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        log.info("Deleting client with id: {}", id);
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
