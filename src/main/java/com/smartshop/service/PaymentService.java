package com.smartshop.service;

import com.smartshop.entity.Payment;

import java.util.List;

public interface PaymentService {

    Payment createPayment(Payment payment);

    Payment getPaymentById(Long id);

    List<Payment> getPaymentsByOrderId(Long orderId);

    List<Payment> getAllPayments();

    Payment updatePayment(Long id, Payment payment);

    void deletePayment(Long id);
}
