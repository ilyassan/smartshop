package com.smartshop.controller;

import com.smartshop.entity.User;
import com.smartshop.exception.UnauthorizedException;
import com.smartshop.service.ClientService;
import jakarta.servlet.http.HttpSession;
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

    private static final String SESSION_USER_KEY = "LOGGED_IN_USER";
    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<User> createClient(@RequestBody User user) {
        log.info("Creating new client with username: {}", user.getUsername());
        User createdClient = clientService.createClient(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getClientById(@PathVariable Long id, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (loggedInUserId == null) {
            throw new UnauthorizedException("Please login first");
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || (!userRole.equals("ADMIN") && !loggedInUserId.equals(id))) {
            throw new UnauthorizedException("You can only view your own profile");
        }

        log.info("Fetching client with id: {}", id);
        User client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllClients(HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can view all clients");
        }

        log.info("Fetching all clients");
        List<User> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateClient(@PathVariable Long id, @RequestBody User user, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (loggedInUserId == null) {
            throw new UnauthorizedException("Please login first");
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || (!userRole.equals("ADMIN") && !loggedInUserId.equals(id))) {
            throw new UnauthorizedException("You can only update your own profile");
        }

        log.info("Updating client with id: {}", id);
        User updatedClient = clientService.updateClient(id, user);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can delete clients");
        }

        log.info("Deleting client with id: {}", id);
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
