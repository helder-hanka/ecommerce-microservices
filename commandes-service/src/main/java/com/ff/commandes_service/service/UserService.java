package com.ff.commandes_service.service;

import com.ff.commandes_service.dto.OrderRequest;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.entity.OrderStatus;
import com.ff.commandes_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final OrderRepository orderRepository;

    public Orders createOrder(OrderRequest orders) {
        var orderToSave = Orders.builder()
                .productId(orders.getProductId())
                .userId(orders.getUserId())
                .quantity(orders.getQuantity())
                .totalPrice(orders.getTotalPrice())
                .orderStatus(OrderStatus.PENDING)
                .orderDate(orders.getOrderDate())
                .orderDate(orders.getOrderDate() != null ? orders.getOrderDate() : java.time.LocalDateTime.now())
                .build();
        return orderRepository.save(orderToSave);
    }
    public Orders getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
    }
    public List<Orders> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Orders> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatus(status);
    }
    public Optional<Orders> updateOrdersByStatus(Long id, OrderStatus orderStatus){
        return Optional.of(orderRepository.findById(id).map(c -> {
            if (c.getOrderStatus() == OrderStatus.CANCELLED || c.getOrderStatus() == OrderStatus.RETURNED) {
                throw new IllegalArgumentException("Order cannot be updated to " + orderStatus + " after it has been " + c.getOrderStatus());
            }
            c.setOrderStatus(orderStatus);
            if (orderStatus == OrderStatus.CANCELLED) {
                c.setCancelledDate(java.time.LocalDateTime.now());
            } else if (orderStatus == OrderStatus.RETURNED) {
                c.setReturnedDate(java.time.LocalDateTime.now());
            }
            return orderRepository.save(c);
        }).orElseThrow(()-> new IllegalArgumentException("Order not found with id: " + id)));
    }
}
