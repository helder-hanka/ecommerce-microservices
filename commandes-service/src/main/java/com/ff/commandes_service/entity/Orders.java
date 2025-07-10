package com.ff.commandes_service.entity;

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
public class Orders {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    private Long adminId;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    @Enumerated(value = jakarta.persistence.EnumType.STRING)
    private OrderStatus orderStatus;
    private LocalDateTime pendingDate;
    private LocalDateTime validatedDate;
    private LocalDateTime cancelledDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime returnedDate;
    private LocalDateTime refundedDate;
    private LocalDateTime orderDate;

}
