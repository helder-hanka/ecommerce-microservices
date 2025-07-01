package com.ff.paiements_service.service;

import com.ff.paiements_service.dto.PaymentPostRequest;
import com.ff.paiements_service.entity.PaymentStatus;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UserPaymentService {

    private final PaymentRepository paymentRepository;

    public Payment createPayment(PaymentPostRequest paymentRequest) {
        if (paymentRequest.getAmount() == null || paymentRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        var payment = Payment.builder()
                .userId(paymentRequest.getUserId())
                .orderId(paymentRequest.getOrderId())
                .paymentMethod(paymentRequest.getPaymentMethod())
                .amount(paymentRequest.getAmount())
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .build();
        return paymentRepository.save(payment);
    }
}
