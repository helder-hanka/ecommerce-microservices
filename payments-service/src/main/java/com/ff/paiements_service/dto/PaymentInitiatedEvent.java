package com.ff.paiements_service.dto;

import com.ff.paiements_service.entity.PaymentMethod;
import com.ff.paiements_service.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInitiatedEvent {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private Long adminId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private LocalDateTime initiatedAt;
    private PaymentStatus status;
}