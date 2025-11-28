package com.smartshop.service.impl;

import com.smartshop.dto.ClientStatistics;
import com.smartshop.dto.UserDTO;
import com.smartshop.entity.Order;
import com.smartshop.entity.Payment;
import com.smartshop.entity.User;
import com.smartshop.enums.CustomerTier;
import com.smartshop.enums.OrderStatus;
import com.smartshop.enums.UserRole;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.mapper.UserMapper;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.PaymentRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.service.ClientService;
import com.smartshop.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientServiceImpl implements ClientService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public UserDTO createClient(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = userMapper.toEntity(userDTO);

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

        UserDTO resultDTO = userMapper.toDTO(savedClient);
        // Don't return password in response
        resultDTO.setPassword(null);
        return resultDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getClientById(Long id) {
        User client = userRepository.findByIdAndRole(id, UserRole.CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        UserDTO userDTO = userMapper.toDTO(client);
        // Don't return password in response
        userDTO.setPassword(null);
        return userDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllClients() {
        List<User> clients = userRepository.findByRole(UserRole.CLIENT);
        return clients.stream()
                .map(user -> {
                    UserDTO dto = userMapper.toDTO(user);
                    dto.setPassword(null); // Don't return passwords
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateClient(Long id, UserDTO userDTO) {
        User existingClient = userRepository.findByIdAndRole(id, UserRole.CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        // Don't update password or username through this method
        userDTO.setPassword(null);
        userDTO.setUsername(null);
        userDTO.setRole(null); // Don't allow role changes

        userMapper.updateEntityFromDTO(userDTO, existingClient);
        User updatedClient = userRepository.save(existingClient);
        log.info("Updated client with id: {}", updatedClient.getId());

        UserDTO resultDTO = userMapper.toDTO(updatedClient);
        resultDTO.setPassword(null);
        return resultDTO;
    }

    @Override
    public void deleteClient(Long id) {
        User client = userRepository.findByIdAndRole(id, UserRole.CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        userRepository.delete(client);
        log.info("Deleted client with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientStatistics getClientStatistics(Long clientId) {
        User client = userRepository.findByIdAndRole(clientId, UserRole.CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        List<Order> allOrders = orderRepository.findByUserId(clientId);

        Integer totalOrders = allOrders.size();
        BigDecimal totalSpent = calculateTotalSpent(clientId);
        BigDecimal totalRemaining = allOrders.stream()
                .map(Order::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime firstOrderDate = allOrders.stream()
                .map(Order::getOrderDate)
                .min(Comparator.naturalOrder())
                .orElse(null);

        LocalDateTime lastOrderDate = allOrders.stream()
                .map(Order::getOrderDate)
                .max(Comparator.naturalOrder())
                .orElse(null);

        Map<OrderStatus, Integer> ordersByStatus = allOrders.stream()
                .collect(Collectors.groupingBy(
                        Order::getStatus,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        List<ClientStatistics.OrderSummary> recentOrders = allOrders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(10)
                .map(order -> ClientStatistics.OrderSummary.builder()
                        .orderId(order.getId())
                        .orderDate(order.getOrderDate())
                        .totalTTC(order.getTotalTTC())
                        .status(order.getStatus())
                        .build())
                .collect(Collectors.toList());

        return ClientStatistics.builder()
                .clientId(client.getId())
                .clientName(client.getName())
                .email(client.getEmail())
                .loyaltyTier(client.getLoyaltyTier())
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .totalRemaining(totalRemaining)
                .firstOrderDate(firstOrderDate)
                .lastOrderDate(lastOrderDate)
                .ordersByStatus(ordersByStatus)
                .recentOrders(recentOrders)
                .build();
    }

    private BigDecimal calculateTotalSpent(Long userId) {
        List<Order> allOrders = orderRepository.findByUserId(userId);
        BigDecimal totalSpending = BigDecimal.ZERO;

        for (Order order : allOrders) {
            List<Payment> payments = paymentRepository.findByOrderId(order.getId());
            for (Payment payment : payments) {
                totalSpending = totalSpending.add(payment.getAmount());
            }
        }

        return totalSpending;
    }
}
