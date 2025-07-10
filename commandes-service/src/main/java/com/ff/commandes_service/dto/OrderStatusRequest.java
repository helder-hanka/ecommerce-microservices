package com.ff.commandes_service.dto;

import com.ff.commandes_service.entity.OrderStatus;
import lombok.Data;

@Data
public class OrderStatusRequest {
    private OrderStatus orderStatus;
}
