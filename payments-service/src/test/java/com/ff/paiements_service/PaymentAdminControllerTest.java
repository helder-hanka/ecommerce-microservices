package com.ff.paiements_service;

import com.fasterxml.jackson.databind.ObjectMapper; // Nécessaire pour convertir les objets en JSON
import com.ff.paiements_service.dto.PaymentStatusPutRequest;
import com.ff.paiements_service.entity.PaymentStatus;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.entity.PaymentMethod;
import com.ff.paiements_service.repository.PaymentRepository; // Injecter le vrai repository
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional; // Pour rollback les transactions

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Charge le contexte Spring Boot complet
@AutoConfigureMockMvc // Configure MockMvc pour les tests de contrôleurs dans un contexte réel
@ActiveProfiles("test") // Active le profil de test pour la configuration de la DB en mémoire
@Transactional // Assure que chaque test est rollbacké après exécution pour un état propre
public class PaymentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc; // Injecte MockMvc configuré par Spring Boot

    @Autowired
    private ObjectMapper objectMapper; // Pour la conversion JSON

    @Autowired
    private PaymentRepository paymentRepository; // Injecte le VRAI repository

    @BeforeEach
    void setUp() {
        // Nettoie la base de données avant chaque test pour s'assurer de l'indépendance
        paymentRepository.deleteAll();
    }

    @Test
    void updatePaymentStatus_ShouldReturnUpdatedPayment() throws Exception {
        // 1. Préparer les données dans la DB (Arrange)
        // Créer un paiement initial dans la base de données
        Payment initialPayment = Payment.builder()
                .userId(1L)
                .orderId(101L)
                .paymentMethod(PaymentMethod.BANK_CARD)
                .amount(BigDecimal.valueOf(100.00))
                .paymentStatus(PaymentStatus.PENDING) // Statut initial
                .adminId(null) // Pas d'admin initialement
                .paymentDate(LocalDateTime.now().minusDays(1))
                .build();
        Payment savedPayment = paymentRepository.save(initialPayment); // Enregistrer le paiement

        // 2. Préparer le DTO de la requête de mise à jour (Act)
        PaymentStatus newStatus = PaymentStatus.COMPLETED;
        Long newAdminId = 789L;

        PaymentStatusPutRequest requestDto = new PaymentStatusPutRequest();
        requestDto.setPaymentStatus(newStatus); // Le nouveau statut
        requestDto.setAdminId(newAdminId); // Le nouvel ID d'admin

        // 3. Exécuter la requête PUT (Act)
        mockMvc.perform(put("/api/admin/payments/order/{paymentId}/status", savedPayment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // 4. Vérifier la réponse (Assert)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPayment.getId()))
                .andExpect(jsonPath("$.paymentStatus").value(newStatus.name()))
                .andExpect(jsonPath("$.adminId").value(newAdminId))
                .andExpect(jsonPath("$.userId").value(savedPayment.getUserId()))
                .andExpect(jsonPath("$.orderId").value(savedPayment.getOrderId()))
                .andExpect(jsonPath("$.amount").value(savedPayment.getAmount()))
                .andExpect(jsonPath("$.paymentMethod").value(savedPayment.getPaymentMethod().name()));
    }
    @Test
    void getAllPaymentsByUserId_ShouldReturnPayments() throws Exception {
        Long userId = 1L;
        // 1. Préparer les données dans la DB
        paymentRepository.save(Payment.builder().userId(userId).orderId(1L).paymentMethod(PaymentMethod.BANK_CARD).amount(BigDecimal.ONE).paymentStatus(PaymentStatus.PENDING).paymentDate(LocalDateTime.now()).build());
        paymentRepository.save(Payment.builder().userId(userId).orderId(2L).paymentMethod(PaymentMethod.PAYPAL).amount(BigDecimal.TEN).paymentStatus(PaymentStatus.COMPLETED).paymentDate(LocalDateTime.now()).build());
        paymentRepository.save(Payment.builder().userId(2L).orderId(3L).paymentMethod(PaymentMethod.BANK_CARD).amount(BigDecimal.valueOf(50)).paymentStatus(PaymentStatus.PENDING).paymentDate(LocalDateTime.now()).build()); // Autre utilisateur

        // 2. Exécuter la requête GET
        mockMvc.perform(get("/api/admin/payments/order/{userId}/all", userId))
                // 3. Vérifier la réponse
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[1].userId").value(userId));
    }
    @Test
    void getAllPayments_ShouldReturnAllPayments() throws Exception {
        // 1. Préparer les données dans la DB
        paymentRepository.save(Payment.builder().userId(1L).orderId(1L).paymentMethod(PaymentMethod.BANK_CARD).amount(BigDecimal.ONE).paymentStatus(PaymentStatus.PENDING).paymentDate(LocalDateTime.now()).build());
        paymentRepository.save(Payment.builder().userId(2L).orderId(2L).paymentMethod(PaymentMethod.PAYPAL).amount(BigDecimal.TEN).paymentStatus(PaymentStatus.COMPLETED).paymentDate(LocalDateTime.now()).build());

        // 2. Exécuter la requête GET
        mockMvc.perform(get("/api/admin/payments/order/all"))
                // 3. Vérifier la réponse
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}