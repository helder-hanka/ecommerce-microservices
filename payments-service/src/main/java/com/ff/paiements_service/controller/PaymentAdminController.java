package com.ff.paiements_service.controller;

import com.ff.paiements_service.dto.PaymentStatusPutRequest;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.service.AdminPaymentService;
import com.ff.paiements_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/payments/order")
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
    @GetMapping("/{userId}/all")
    public List<Payment> getAllPaymentsByUserId(@PathVariable Long userId) {
        return paymentService.getAllPaymentsByUserId(userId);
    }
    @GetMapping("/{orderId}")
    public List<Payment> getPaymentsByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }
    @GetMapping("/all")
    public List<Payment> getAllPayments() {
        return adminPaymentService.getAllPayments();
    }
}
