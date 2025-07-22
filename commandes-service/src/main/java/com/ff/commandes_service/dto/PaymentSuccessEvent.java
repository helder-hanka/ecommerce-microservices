package com.ff.commandes_service.dto;

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
    private String status; // CONFIRMED
}
