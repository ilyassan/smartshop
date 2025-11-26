package com.smartshop.service;

import com.smartshop.dto.PaymentDTO;

import java.util.List;

public interface PaymentService {

    PaymentDTO createPayment(PaymentDTO paymentDTO);

    PaymentDTO getPaymentById(Long id);

    List<PaymentDTO> getPaymentsByOrderId(Long orderId);

    List<PaymentDTO> getAllPayments();

    PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO);

    void deletePayment(Long id);
}
