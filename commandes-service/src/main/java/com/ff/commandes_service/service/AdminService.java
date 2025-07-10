package com.ff.commandes_service.service;

import com.ff.commandes_service.dto.OrderStatusRequest;
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

    public Optional<Orders> getOrderByIdByAdmin(Long id, Long adminId) {
        Optional<Orders> order = orderRepository.findById(id)
        .filter(orders -> orders.getAdminId().equals(adminId));
        if (order.isEmpty()) {
            throw new IllegalArgumentException("Order not found with id: " + id + " for admin with id: " + adminId);
        }
        return order;
    }

    public List<Orders> getAllOrdersByAdmin(Long adminId) {
        return  orderRepository.findAllProductByAdminId(adminId);
    }

    public long countOrders() {
        return orderRepository.count();
    }
    public Optional<Orders> updateOrderStatus(Long id,Long admin, OrderStatusRequest orderStatus){
        return Optional.of(orderRepository.findById(id).map(order -> {
            if (!order.getAdminId().equals(admin)) {
                throw new IllegalArgumentException("Order with id: " + id + " does not belong to admin with id: " + admin);
            }
            if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.RETURNED) {
                throw new IllegalArgumentException("Order cannot be updated to " + orderStatus + " after it has been " + order.getOrderStatus());
            }
            order.setOrderStatus(orderStatus.getOrderStatus());
            switch (orderStatus.getOrderStatus()) {
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
