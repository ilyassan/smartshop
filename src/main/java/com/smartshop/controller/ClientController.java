package com.smartshop.controller;

import com.smartshop.annotation.RequireAuth;
import com.smartshop.annotation.RequireRole;
import com.smartshop.dto.ClientStatistics;
import com.smartshop.dto.UserDTO;
import com.smartshop.exception.UnauthorizedException;
import com.smartshop.service.ClientService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<UserDTO> createClient(@Valid @RequestBody UserDTO user) {
        log.info("Creating new client with username: {}", user.getUsername());
        UserDTO createdClient = clientService.createClient(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
    }

    @GetMapping("/{id}")
    @RequireAuth
    public ResponseEntity<UserDTO> getClientById(@PathVariable Long id, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        if (!userRole.equals("ADMIN") && !loggedInUserId.equals(id)) {
            throw new UnauthorizedException("You can only view your own profile");
        }

        log.info("Fetching client with id: {}", id);
        UserDTO client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @GetMapping
    @RequireRole("ADMIN")
    public ResponseEntity<List<UserDTO>> getAllClients() {
        log.info("Fetching all clients");
        List<UserDTO> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @PutMapping("/{id}")
    @RequireAuth
    public ResponseEntity<UserDTO> updateClient(@PathVariable Long id, @Valid @RequestBody UserDTO user, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        if (!userRole.equals("ADMIN") && !loggedInUserId.equals(id)) {
            throw new UnauthorizedException("You can only update your own profile");
        }

        log.info("Updating client with id: {}", id);
        UserDTO updatedClient = clientService.updateClient(id, user);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        log.info("Deleting client with id: {}", id);
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/statistics")
    @RequireAuth
    public ResponseEntity<ClientStatistics> getClientStatistics(@PathVariable Long id, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        if (!userRole.equals("ADMIN") && !loggedInUserId.equals(id)) {
            throw new UnauthorizedException("You can only view your own statistics");
        }

        log.info("Fetching statistics for client with id: {}", id);
        ClientStatistics statistics = clientService.getClientStatistics(id);
        return ResponseEntity.ok(statistics);
    }
}
