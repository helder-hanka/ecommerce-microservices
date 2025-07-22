package com.ff.paiements_service.repository;

import com.ff.paiements_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);

    //List<Payment> findByOrderId(Long orderId);
    Optional<Payment>findByOrderId(Long orderId);

    List<Payment> findByAdminId(Long adminId);

    Optional<Payment> findByOrderIdAndAdminId(Long orderId, Long adminId);
}
