package com.ff.paiements_service.controller;

import com.ff.paiements_service.dto.PaymentPostRequest;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.service.PaymentService;
import com.ff.paiements_service.service.UserPaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/user/payments/order")
public class PaymentUserController {

    private final UserPaymentService userPaymentService;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity <Payment> createPayment(@Valid @RequestBody PaymentPostRequest paymentPostRequest) {
        Payment pPostR = userPaymentService.createPayment(paymentPostRequest);
        return ResponseEntity.ok(pPostR);
    }
     @GetMapping("/{id}")
     public Payment getUserPayments(@PathVariable Long id) {
        return paymentService.findPaymentById(id);
    }
    @GetMapping("/user/{userId}/all")
    public List<Payment> getAllPayments(@PathVariable Long userId) {
        return paymentService.getAllPaymentsByUserId(userId);
    }
    @GetMapping("/{orderId}/all")
    public List<Payment> getPaymentsByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }
 }
