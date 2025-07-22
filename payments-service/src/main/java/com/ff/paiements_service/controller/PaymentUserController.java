package com.ff.paiements_service.controller;

import com.ff.paiements_service.dto.PaymentPostRequest;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.service.PaymentService;
import com.ff.paiements_service.service.UserPaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import com.ff.paiements_service.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/payments/user")
public class PaymentUserController {

    private final UserPaymentService userPaymentService;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity <Payment> createPayment(@Valid @RequestBody PaymentPostRequest paymentPostRequest) {
        Long userId = SecurityUtils.getCurrentUserId();
        Payment pPostR = userPaymentService.createPayment(userId, paymentPostRequest);
        return ResponseEntity.ok(pPostR);
    }

    @GetMapping("/{id}")
     public Payment getUserPayments(@PathVariable Long id) {
        return paymentService.findPaymentById(id);
    }
    @GetMapping
    public List<Payment> getAllPayments() {
        Long userId = SecurityUtils.getCurrentUserId();
        return paymentService.getAllPaymentsByUserId(userId);
    }
    @GetMapping("/orderId/{orderId}")
    public Optional<Payment> getPaymentsByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentsByOrderId(orderId);
    }
 }
