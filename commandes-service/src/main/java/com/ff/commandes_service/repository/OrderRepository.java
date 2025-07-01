package com.ff.commandes_service.repository;

import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    // Custom query methods can be defined here if needed
    // For example, to find orders by userId or productId
    List<Orders> findByUserId(Long userId);

    List<Orders> findByProductId(Long productId);

    List<Orders> findByOrderStatus(OrderStatus orderStatus);

    long countByOrderStatus(OrderStatus status);

    // Additional methods can be added as required
}
