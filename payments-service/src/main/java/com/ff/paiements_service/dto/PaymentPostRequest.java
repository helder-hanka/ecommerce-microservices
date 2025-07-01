package com.ff.paiements_service.dto;

import com.ff.paiements_service.entity.PaymentStatus;
import com.ff.paiements_service.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
public class PaymentPostRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;
    @NotNull(message = "Order ID cannot be null")
    private Long orderId;
    @NotNull(message = "Payment Method cannot be null")
    private PaymentMethod PaymentMethod;
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
}
