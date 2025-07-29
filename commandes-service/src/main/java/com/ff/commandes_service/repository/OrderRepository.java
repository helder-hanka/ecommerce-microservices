package com.ff.commandes_service.repository;

import com.ff.commandes_service.entity.Orders;
import com.ff.commandes_service.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    long countByOrderStatus(OrderStatus status);
    List<Orders> findByUserId(Long userId);
}
