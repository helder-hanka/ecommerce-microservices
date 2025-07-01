package com.ff.commandes_service.service;

import com.ff.commandes_service.entity.OrderStatus;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final OrderRepository orderRepository;

    public Optional<Orders> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<List<Orders>> getAllOrders() {
        return Optional.ofNullable(Optional.of(orderRepository.findAll()).orElseThrow(() -> new IllegalArgumentException("No orders found")));
    }

    public long countOrders() {
        return orderRepository.count();
    }
    public Optional<Orders> updateOrderStatus(Long id, OrderStatus status){
        return Optional.of(orderRepository.findById(id).map(order -> {
            if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.RETURNED) {
                throw new IllegalArgumentException("Order cannot be updated to " + status + " after it has been " + order.getOrderStatus());
            }
            order.setOrderStatus(status);
            switch (status) {
                case PENDING, VALIDATED -> order.setOrderDate(java.time.LocalDateTime.now());
                case CANCELLED -> order.setCancelledDate(java.time.LocalDateTime.now());
                case RETURNED -> order.setReturnedDate(java.time.LocalDateTime.now());
                case REFUNDED -> order.setRefundedDate(java.time.LocalDateTime.now());
                case SHIPPED -> order.setShippedDate(java.time.LocalDateTime.now());
                case DELIVERED -> order.setDeliveredDate(java.time.LocalDateTime.now());
                default -> {
                }
            }
            return orderRepository.save(order);
        }).orElseThrow(()-> new IllegalArgumentException("Order not found with id: " + id)));
    }
    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByOrderStatus(status);
    }
    public long countOrdersByStatus(String status) {
        return orderRepository.countByOrderStatus(OrderStatus.valueOf(status.toUpperCase()));
    }
    public long countAllOrders() {
        return orderRepository.count();
    }

}
