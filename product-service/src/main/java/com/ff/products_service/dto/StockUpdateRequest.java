package com.ff.products_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockUpdateRequest {
//    private Long productId;
//    private Long adminId;
    @NotNull(message = "Product ID cannot be null")
    private int quantity;
}
