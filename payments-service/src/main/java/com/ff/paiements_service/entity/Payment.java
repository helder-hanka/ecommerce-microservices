package com.ff.paiements_service.entity;

import com.ff.paiements_service.converter.PayementStatusConverter;
import com.ff.paiements_service.converter.PaymentMethodConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    private Long adminId;
    @Column(nullable = false)
    private Long orderId;
    @Convert(converter = PaymentMethodConverter.class)
    private PaymentMethod paymentMethod;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Convert(converter = PayementStatusConverter.class)
    private PaymentStatus paymentStatus;
    @Column(nullable = false)
    private LocalDateTime paymentDate;
}
