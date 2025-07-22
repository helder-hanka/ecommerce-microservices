package com.ff.paiements_service.dto;

import com.ff.paiements_service.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentFailedEvent {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private Long adminId;
    private PaymentStatus status; // FAILED
    private LocalDateTime failedAt;
    private String reason;
}
