package com.ff.commandes_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductStockResponse {
    private String message;
    private int Status;
    private LocalDateTime timestamp;
    private Long id;
    private Long productAdminId;
    private Integer stock;

}
