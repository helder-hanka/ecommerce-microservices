package com.ff.commandes_service.dto;

import lombok.Data;

@Data
public class CountOrdersResponse {
    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private long cancelledOrders;
}
