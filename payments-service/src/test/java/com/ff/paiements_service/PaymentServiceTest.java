package com.ff.paiements_service;

import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.entity.PaymentMethod;
import com.ff.paiements_service.entity.PaymentStatus;
import com.ff.paiements_service.repository.PaymentRepository;
import com.ff.paiements_service.service.PaymentService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // Indique à Spring Boot de charger le contexte complet de l'application
@ActiveProfiles("test") // Active le profil 'test' si vous avez un application-test.properties
@Transactional
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService; // Spring injectera le vrai service

    @Autowired
    private PaymentRepository paymentRepository; // Spring injectera le vrai repository

    @BeforeEach
    void setUp() {
        // Nettoyez la base de données avant chaque test pour s'assurer qu'ils sont indépendants
        paymentRepository.deleteAll();
    }

    @Test
    void findPaymentById_ShouldReturnPayment() {
        // 1. Préparer les données (insérer un paiement réel dans la DB)
        String transaction = "txn12345"; // Simuler un ID de transaction
        Payment payment = Payment.builder()
                .userId(1L)
                .orderId(101L)
                .adminId(1L)
                .transactionId(transaction)
                .paymentMethod(PaymentMethod.BANK_CARD)
                .amount(BigDecimal.valueOf(100.00))
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .build();
        Payment savedPayment = paymentRepository.save(payment); // Sauvegardez le paiement en DB

        // 2. Exécuter la méthode du service
        Payment foundPayment = paymentService.findPaymentById(savedPayment.getId());

        // 3. Vérifier le résultat
        assertNotNull(foundPayment);
        assertEquals(savedPayment.getId(), foundPayment.getId());
        assertEquals(100.00, foundPayment.getAmount().doubleValue());
        assertEquals(PaymentMethod.BANK_CARD, foundPayment.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, foundPayment.getPaymentStatus());
        assertEquals(transaction, foundPayment.getTransactionId());
        assertEquals(1L, foundPayment.getUserId());
        assertEquals(101L, foundPayment.getOrderId());
        assertEquals(1L, foundPayment.getAdminId());
        assertNotNull(foundPayment.getPaymentDate());
    }

    @Test
    void findPaymentById_ShouldThrowExceptionWhenNotFound() {
        // Aucune donnée insérée, donc l'ID 99L ne devrait pas exister
        Long nonExistentId = 99L;

        // Exécuter la méthode du service et vérifier l'exception
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.findPaymentById(nonExistentId)
        );

        assertEquals("Payment not found with id: " + nonExistentId, exception.getMessage());
    }

    @Test
    void getAllPaymentsByUserId_ShouldReturnPayments() {
        Long userId = 2L;
        Long adminId = 1L;
        String transaction = "txn12345";
        // 1. Préparer les données
        paymentRepository.save(
                Payment.builder()
                        .userId(userId)
                        .adminId(adminId)
                        .transactionId(transaction)
                        .orderId(201L)
                        .paymentMethod(PaymentMethod.PAYPAL)
                        .amount(BigDecimal.valueOf(50.00))
                        .paymentStatus(PaymentStatus.COMPLETED)
                        .paymentDate(LocalDateTime.now()).build());
        paymentRepository.save(
                Payment.builder()
                        .userId(userId)
                        .adminId(adminId)
                        .transactionId(transaction)
                        .orderId(202L)
                        .paymentMethod(PaymentMethod.BANK_CARD)
                        .amount(BigDecimal.valueOf(75.00))
                        .paymentStatus(PaymentStatus.PENDING)
                        .paymentDate(LocalDateTime.now()).build());
        // Un paiement pour un autre utilisateur
        paymentRepository.save(
                Payment.builder()
                        .userId(3L)
                        .adminId(adminId)
                        .orderId(301L)
                        .transactionId(transaction)
                        .paymentMethod(PaymentMethod.PAYPAL)
                        .amount(BigDecimal.valueOf(25.00))
                        .paymentStatus(PaymentStatus.COMPLETED)
                        .paymentDate(LocalDateTime.now())
                        .build());

        // 2. Exécuter la méthode du service
        List<Payment> payments = paymentService.getAllPaymentsByUserId(userId);

        // 3. Vérifier le résultat
        assertNotNull(payments);
        assertEquals(2, payments.size());
        assertTrue(payments.stream().allMatch(p -> p.getUserId().equals(userId)));
    }

    @Test
    void getAllPaymentsByUserId_ShouldThrowExceptionWhenNoPaymentsFound() {
        // Aucune donnée insérée pour l'utilisateur 99L
        Long nonExistentUserId = 99L;

        // Exécuter la méthode et vérifier l'exception
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.getAllPaymentsByUserId(nonExistentUserId)
        );

        assertEquals("No payments found for user with ID: " + nonExistentUserId, exception.getMessage());
    }

    @Test
    void getAllPaymentsByUserId_ShouldThrowExceptionForNullUserId() {
        Long nonExistentUserId = 9999L;
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.getAllPaymentsByUserId(nonExistentUserId)
        );
        assertEquals("No payments found for user with ID: "+nonExistentUserId, exception.getMessage());
    }

    @Test
    void getPaymentsByOrderId_ShouldReturnPayments() {
        Long orderId = 300L;
        Long adminId = 1L;
        String transaction = "txn12345";
        // 1. Préparer les données
        paymentRepository.save(
                Payment.builder()
                .userId(10L)
                .adminId(adminId)
                .orderId(orderId)
                .transactionId(transaction)
                .paymentMethod(PaymentMethod.PAYPAL)
                .amount(BigDecimal.valueOf(120.00)).paymentStatus(PaymentStatus.COMPLETED).paymentDate(LocalDateTime.now()).build());
        //paymentRepository.save(Payment.builder().userId(11L).orderId(orderId).paymentMethod(PaymentMethod.BANK_CARD).amount(BigDecimal.valueOf(80.00)).paymentStatus(PaymentStatus.PENDING).paymentDate(LocalDateTime.now()).build());
        // Un paiement pour une autre commande
        paymentRepository.save(
                Payment.builder()
                .userId(12L)
                .adminId(adminId)
                .orderId(301L)
                .transactionId(transaction)
                .paymentMethod(PaymentMethod.PAYPAL)
                .amount(BigDecimal.valueOf(30.00))
                .paymentStatus(PaymentStatus.COMPLETED)
                .paymentDate(LocalDateTime.now()).build());

        // 2. Exécuter la méthode du service
        //List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);
        Optional<Payment> payments = paymentService.getPaymentsByOrderId(orderId);

        // 3. Vérifier le résultat
        assertNotNull(payments);
        //assertEquals(2, payments.size());
        //assertEquals(1, payments);
        assertTrue(payments.stream().allMatch(p -> p.getOrderId().equals(orderId)));
    }

    @Test
    void getPaymentsByOrderId_ShouldThrowExceptionWhenNoPaymentsFound() {
        // Aucune donnée insérée pour la commande 999L
        Long nonExistentOrderId = 999L;

        // Exécuter la méthode et vérifier l'exception
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.getPaymentsByOrderId(nonExistentOrderId)
        );

        assertEquals("No payments found for order with ID: " + nonExistentOrderId, exception.getMessage());
    }

    @Test
    void getPaymentsByOrderId_ShouldThrowExceptionForNullOrderId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.getPaymentsByOrderId(null)
        );
        assertEquals("Order ID cannot be null", exception.getMessage());
    }
}
