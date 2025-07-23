package com.ff.commandes_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ff.commandes_service.dto.OrderCreatedEvent;
import com.ff.commandes_service.dto.OrderRequest;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.entity.OrderStatus;
import com.ff.commandes_service.repository.OrderRepository;
import com.ff.commandes_service.service.kafka.OrderKafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final OrderRepository orderRepository;
    private final OrderKafkaProducer kafkaOrderProducer;

    @Transactional
    public Orders createOrder(Long userId, OrderRequest orders) throws JsonProcessingException {


        if (orders.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (orders.getTotalPrice() == null || orders.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total price must be greater than zero");
        }

        var orderToSave = Orders.builder()
                .productId(orders.getProductId())
                .userId(userId)
                .adminId(orders.getAdminId())
                .quantity(orders.getQuantity())
                .totalPrice(orders.getTotalPrice())
                .orderStatus(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .build();
        Orders savedOrder = orderRepository.save(orderToSave);
        // Send order to Kafka topic
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getAdminId(),
                savedOrder.getOrderStatus().name(),
                savedOrder.getTotalPrice(),
                savedOrder.getOrderDate(),
                List.of(new OrderCreatedEvent.OrderItemEvent(
                        savedOrder.getProductId(),
                        savedOrder.getQuantity(),
                        savedOrder.getTotalPrice()
                ))
        );
        kafkaOrderProducer.sendOrderCreatedEvent(orderCreatedEvent);
        return savedOrder;
        //return orderRepository.save(orderToSave);
    }
    public Orders getOrderById(Long id, Long userId) {
        return orderRepository.findById(id).filter(order-> order.getUserId().equals(userId)).orElseThrow();
    }
    public List<Orders> getAllOrders(Long userId) {
        List<Orders>orders = orderRepository.findByUserId(userId);
        if (orders.isEmpty()) {
            throw new IllegalArgumentException("No orders found for user with id: " + userId);
        }
        return orders;
    }

    public List<Orders> getOrdersByStatus(Long userId,OrderStatus status) {
        // find orders by userId and status
        List<Orders> orders = orderRepository.findByUserId(userId).stream().filter(order-> order.getOrderStatus() == status).toList();
        if (orders.isEmpty()) {
            throw new IllegalArgumentException("No orders found for user with id: " + userId + " and status: " + status);
        }
        return orders;
    }
    public Optional<Orders> updateOrdersByStatus(Long id,Long userId, OrderStatus orderStatus){
        // find order by id and userId
        return Optional.of(orderRepository.findById(id).filter(order -> order.getUserId().equals(userId)).map(c -> {
            if (c.getOrderStatus() == OrderStatus.CANCELLED || c.getOrderStatus() == OrderStatus.RETURNED) {
                throw new IllegalArgumentException("Order cannot be updated to " + orderStatus + " after it has been " + c.getOrderStatus());
            }
            c.setOrderStatus(orderStatus);
            if (orderStatus == OrderStatus.CANCELLED) {
                c.setCancelledDate(LocalDateTime.now());
            } else if (orderStatus == OrderStatus.RETURNED) {
                c.setReturnedDate(LocalDateTime.now());
            }
            return orderRepository.save(c);
        }).orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id + " for user with id: " + userId)));
    }
}
