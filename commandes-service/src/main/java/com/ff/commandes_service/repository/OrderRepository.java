package com.ff.commandes_service.repository;

import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByOrderStatus(OrderStatus orderStatus);
    long countByOrderStatus(OrderStatus status);
    List<Orders> findAllProductByAdminId(Long adminId);
}
