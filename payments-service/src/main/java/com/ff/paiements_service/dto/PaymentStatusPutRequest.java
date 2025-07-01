package com.ff.paiements_service.dto;

import com.ff.paiements_service.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentStatusPutRequest {
    @NotNull(message = "Admin ID cannot be null")
    private Long adminId;
    @NotNull(message = "Payment Status cannot be null")
    private PaymentStatus paymentStatus;
}
