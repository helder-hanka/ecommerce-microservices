package com.ff.commandes_service.service;

import com.ff.commandes_service.dto.CountOrdersResponse;
import com.ff.commandes_service.dto.OrderItemResponse;
import com.ff.commandes_service.dto.OrderResponse;
import com.ff.commandes_service.dto.OrderStatusRequest;
import com.ff.commandes_service.entity.OrderItem;
import com.ff.commandes_service.entity.OrderStatus;
import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.repository.OrderItemRepository;
import com.ff.commandes_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public Optional<Orders> getOrderByIdByAdmin(Long id, Long adminId) {
        Optional<Orders> order = orderRepository.findById(id)
        //.filter(orders -> orders.getAdminId().equals(adminId));
        .filter(orders -> orders.getItems().stream()
                .anyMatch(item -> item.getAdminId().equals(adminId)));
        if (order.isEmpty()) {
            throw new IllegalArgumentException("Order not found with id: " + id + " for admin with id: " + adminId);
        }
        return order;
    }
    @Transactional
    public List<OrderResponse> getAllOrdersByAdmin(Long adminId) {
        //return  orderRepository.findAllProductByAdminId(adminId);
         //List<Orders>orderItem = orderRepository.findOrdersByOrderItemAdminId(adminId);
        List<OrderItem> orderItem = orderItemRepository.findByAdminId(adminId);
        if (orderItem.isEmpty()) {
            throw new IllegalArgumentException("No orders found for admin with id: " + adminId);
        }
        // Grouping order items by order ID
        Map<Orders, List<OrderItem>> groupedByOrder = orderItem.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrder));

        List<OrderResponse>responses = new ArrayList<>();

        for (Map.Entry<Orders, List<OrderItem>> entry : groupedByOrder.entrySet()) {
            Orders associatedOrder = entry.getKey();
            List<OrderItem> itemsForThisOrder = entry.getValue();
            OrderResponse response= OrderResponse.builder()
                    .userId(associatedOrder.getUserId())
                    .orderId(associatedOrder.getId())
                    .status(associatedOrder.getOrderStatus().name())
                    .totalAmount(associatedOrder.getTotalPrice())
                    .createdAt(associatedOrder.getOrderDate())
                    .items(itemsForThisOrder.stream()
                            .map(item -> new OrderItemResponse(
                                    item.getId(),
                                    item.getProductId(),
                                    item.getAdminId(),
                                    item.getName(),
                                    item.getQuantity(),
                                    item.getPrice()))
                            .collect(Collectors.toList()))
                    .build();
            responses.add(response);
        }
        return responses;
    }

    public CountOrdersResponse countOrders(Long adminId) {
        // Counting total orders,  pendingOrders, completedOrders,cancelledOrders by admin ID
        long totalOrders = orderRepository.count();
        if (totalOrders == 0) {
            throw new IllegalArgumentException("No orders found for admin with id: " + adminId);
        }
        long pendingOrders = orderRepository.countByOrderStatus(OrderStatus.PENDING);
        long completedOrders = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByOrderStatus(OrderStatus.CANCELLED);
        CountOrdersResponse response = new CountOrdersResponse();
        response.setTotalOrders(totalOrders);
        response.setPendingOrders(pendingOrders);
        response.setCompletedOrders(completedOrders);
        response.setCancelledOrders(cancelledOrders);
        return response;
    }
    public Optional<Orders> updateOrderStatus(Long id,Long admin, OrderStatusRequest orderStatus){
        return Optional.of(orderRepository.findById(id).map(order -> {
            if (order.getItems().stream()
                    .noneMatch(item -> item.getAdminId().equals(admin))) {
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
}
