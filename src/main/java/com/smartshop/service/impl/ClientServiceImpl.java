package com.smartshop.service.impl;

import com.smartshop.entity.User;
import com.smartshop.enums.CustomerTier;
import com.smartshop.enums.UserRole;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.UserRepository;
import com.smartshop.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientServiceImpl implements ClientService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createClient(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Force role to CLIENT
        user.setRole(UserRole.CLIENT);

        // Set default loyalty tier if not provided
        if (user.getLoyaltyTier() == null) {
            user.setLoyaltyTier(CustomerTier.BASIC);
        }

        User savedClient = userRepository.save(user);
        log.info("Created new client with username: {}", savedClient.getUsername());

        return savedClient;
    }

    @Override
    @Transactional(readOnly = true)
    public User getClientById(Long id) {
        return userRepository.findByIdAndRole(id, UserRole.CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllClients() {
        return userRepository.findByRole(UserRole.CLIENT);
    }

    @Override
    public User updateClient(Long id, User user) {
        User existingClient = userRepository.findByIdAndRole(id, UserRole.CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        // Update only allowed fields
        if (user.getName() != null) {
            existingClient.setName(user.getName());
        }
        if (user.getEmail() != null) {
            existingClient.setEmail(user.getEmail());
        }
        if (user.getPhone() != null) {
            existingClient.setPhone(user.getPhone());
        }
        if (user.getAddress() != null) {
            existingClient.setAddress(user.getAddress());
        }
        if (user.getLoyaltyTier() != null) {
            existingClient.setLoyaltyTier(user.getLoyaltyTier());
        }

        User updatedClient = userRepository.save(existingClient);
        log.info("Updated client with id: {}", updatedClient.getId());

        return updatedClient;
    }

    @Override
    public void deleteClient(Long id) {
        User client = userRepository.findByIdAndRole(id, UserRole.CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        userRepository.delete(client);
        log.info("Deleted client with id: {}", id);
    }
}
