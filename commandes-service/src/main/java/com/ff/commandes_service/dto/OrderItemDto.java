package com.ff.commandes_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDto {
    private Long id;
    @NotNull(message = "Admin ID cannot be blank")
    private Long adminId;
    @NotNull(message = "Product ID cannot be null")
    private Long productId;
    @NotNull(message = "Name cannot be null")
    private String name;
    @NotNull(message = "Product ID cannot be null")
    private int quantity;
    @NotNull(message = "Price cannot be null")
    private BigDecimal price;
}
