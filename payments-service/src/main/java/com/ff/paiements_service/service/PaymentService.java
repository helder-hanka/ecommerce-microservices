package com.ff.paiements_service.service;

import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + paymentId));
    }
    public List <Payment> getAllPaymentsByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        List<Payment> payments = paymentRepository.findByUserId(userId);
        if (payments.isEmpty()) {
            throw new IllegalArgumentException("No payments found for user with ID: " + userId);
        }
        return payments;
    }
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        if (payments.isEmpty()) {
            throw new IllegalArgumentException("No payments found for order with ID: " + orderId);
        }
        return payments;
    }
}
