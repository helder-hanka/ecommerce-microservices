package com.ff.products_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockUpdateRequest {
    private Long productId;
    private Long adminId;
    private int quantity;
}
