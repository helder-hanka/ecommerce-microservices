package com.ff.paiements_service.controller;

import com.ff.paiements_service.dto.PaymentStatusPutRequest;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.security.SecurityUtils;
import com.ff.paiements_service.service.AdminPaymentService;
import com.ff.paiements_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/payments/admin")
public class PaymentAdminController {
    private final AdminPaymentService adminPaymentService;
    private final PaymentService paymentService;

     @PutMapping("/{paymentId}/status")
     public ResponseEntity <Payment> updatePaymentStatus(@PathVariable Long paymentId, @Valid @RequestBody PaymentStatusPutRequest status) {
         Payment updtStatusPayment = adminPaymentService.updatePaymentStatus(paymentId, status);
            return ResponseEntity.ok(updtStatusPayment);
     }
    @GetMapping("/{paymentId}")
    public Payment getPaymentById(@PathVariable Long paymentId) {
        return paymentService.findPaymentById(paymentId);
    }
    @GetMapping
    public List<Payment> getAllPaymentsByAdminId() {
        Long adminId = SecurityUtils.getCurrentUserId();
        return adminPaymentService.getAllPaymentsByAdminId(adminId);
    }
    @GetMapping("orderId/{orderId}")
    public Optional<Payment> getPaymentsByOrderId(@PathVariable Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
         Long adminId = SecurityUtils.getCurrentUserId();
        return adminPaymentService.getPaymentsByOrderIdByAdminId(orderId, adminId);
    }
}
