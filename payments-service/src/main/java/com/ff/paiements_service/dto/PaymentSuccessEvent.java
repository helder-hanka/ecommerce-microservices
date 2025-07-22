package com.ff.paiements_service.dto;

import com.ff.paiements_service.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSuccessEvent {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private Long adminId;
    private BigDecimal amount;
    private String transactionId; // ID de transaction de la passerelle de paiement
    private LocalDateTime paidAt;
    private PaymentStatus status; // CONFIRMED
}
