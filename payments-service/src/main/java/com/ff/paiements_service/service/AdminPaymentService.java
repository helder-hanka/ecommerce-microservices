package com.ff.paiements_service.service;

import com.ff.paiements_service.dto.PaymentStatusPutRequest;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AdminPaymentService {
    private final PaymentRepository paymentRepository;

    public Payment updatePaymentStatus(Long paymentId, PaymentStatusPutRequest status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + paymentId));
        payment.setPaymentStatus(status.getPaymentStatus());
        payment.setAdminId(status.getAdminId());
        payment.setPaymentDate(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    public List<Payment> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        if (payments.isEmpty()) {
            throw new IllegalArgumentException("No payments found");
        }
        return payments;
    }
}
