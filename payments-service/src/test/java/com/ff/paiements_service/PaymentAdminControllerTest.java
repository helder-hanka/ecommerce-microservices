package com.ff.paiements_service;

import com.fasterxml.jackson.databind.ObjectMapper; // Nécessaire pour convertir les objets en JSON
import com.ff.paiements_service.dto.PaymentStatusPutRequest;
import com.ff.paiements_service.entity.PaymentStatus;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.entity.PaymentMethod;
import com.ff.paiements_service.repository.PaymentRepository; // Injecter le vrai repository
import com.ff.paiements_service.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional; // Pour rollback les transactions

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

    private final Long TEST_ADMIN_ID = 789L;
    private final String TEST_TRANSACTION_ID = "txn12345";
    private final String TEST_ADMIN_USERNAME = "adminUser";
    private final String TEST_ADMIN_ROLE = "ADMIN";
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
                .adminId(TEST_ADMIN_ID)
                .transactionId(TEST_TRANSACTION_ID)
                .paymentMethod(PaymentMethod.BANK_CARD)
                .amount(BigDecimal.valueOf(100.00))
                .paymentStatus(PaymentStatus.PENDING) // Statut initial
                .paymentDate(LocalDateTime.now().minusDays(1))
                .build();
        Payment savedPayment = paymentRepository.save(initialPayment); // Enregistrer le paiement

        // 2. Préparer le DTO de la requête de mise à jour (Act)
        PaymentStatus newStatus = PaymentStatus.COMPLETED;

        PaymentStatusPutRequest requestDto = new PaymentStatusPutRequest();
        requestDto.setPaymentStatus(newStatus); // Le nouveau statut
        requestDto.setAdminId(TEST_ADMIN_ID); // Le nouvel ID d'admin

        // 3. Exécuter la requête PUT (Act)
        mockMvc.perform(put("/api/payments/admin/{paymentId}/status", savedPayment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf())
                        .with(user(TEST_ADMIN_USERNAME).roles(TEST_ADMIN_ROLE))) // Simuler l'authentification de l'admin)

                // 4. Vérifier la réponse (Assert)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPayment.getId()))
                .andExpect(jsonPath("$.userId").value(savedPayment.getUserId()))
                .andExpect(jsonPath("$.adminId").value(TEST_ADMIN_ID))
                .andExpect(jsonPath("$.orderId").value(savedPayment.getOrderId()))
                .andExpect(jsonPath("$.transactionId").value(TEST_TRANSACTION_ID))
                .andExpect(jsonPath("$.amount").value(savedPayment.getAmount()))
                .andExpect(jsonPath("$.paymentStatus").value(newStatus.name()))
                .andExpect(jsonPath("$.paymentMethod").value(savedPayment.getPaymentMethod().name()));
    }

    // APP
    @Test
    void getPaymentById_Admin_ShouldReturnPayment()throws Exception{
        // 1. Préparer les données dans la DB
        Payment payment = Payment.builder()
                .userId(1L)
                .orderId(101L)
                .adminId(TEST_ADMIN_ID)
                .transactionId(TEST_TRANSACTION_ID)
                .paymentMethod(PaymentMethod.BANK_CARD)
                .amount(BigDecimal.valueOf(100.00))
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .build();
        Payment savedPayment = paymentRepository.save(payment); // Enregistrer le paiement

        // 2. Exécuter la requête GET
        mockMvc.perform(get("/api/payments/admin/{paymentId}", savedPayment.getId())
                        .with(csrf())
                        .with(user(TEST_ADMIN_USERNAME).roles(TEST_ADMIN_ROLE))) // Simuler l'authentification de l'admin
                // 3. Vérifier la réponse
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPayment.getId()))
                .andExpect(jsonPath("$.userId").value(savedPayment.getUserId()))
                .andExpect(jsonPath("$.adminId").value(TEST_ADMIN_ID))
                .andExpect(jsonPath("$.orderId").value(savedPayment.getOrderId()))
                .andExpect(jsonPath("$.transactionId").value(TEST_TRANSACTION_ID))
                .andExpect(jsonPath("$.amount").value(savedPayment.getAmount()))
                .andExpect(jsonPath("$.paymentStatus").value(savedPayment.getPaymentStatus().name()))
                .andExpect(jsonPath("$.paymentMethod").value(savedPayment.getPaymentMethod().name()));
    }
    @Test
    void getAllPaymentsByAdminId_ShouldReturnPayments() throws Exception{
        // 1. Préparer les données dans la DB
        // Paiements pour l'admin de test
        paymentRepository.save(Payment.builder().userId(1L).orderId(101L).adminId(TEST_ADMIN_ID).transactionId("txn_admin_1").paymentMethod(PaymentMethod.BANK_CARD).amount(BigDecimal.valueOf(10.00)).paymentStatus(PaymentStatus.PENDING).paymentDate(LocalDateTime.now()).build());
        paymentRepository.save(Payment.builder().userId(2L).orderId(102L).adminId(TEST_ADMIN_ID).transactionId("txn_admin_2").paymentMethod(PaymentMethod.PAYPAL).amount(BigDecimal.valueOf(20.00)).paymentStatus(PaymentStatus.COMPLETED).paymentDate(LocalDateTime.now()).build());
        // Paiement pour un autre admin (ne devrait pas être retourné)
        paymentRepository.save(Payment.builder().userId(3L).orderId(103L).adminId(999L).transactionId("txn_other_admin").paymentMethod(PaymentMethod.BANK_CARD).amount(BigDecimal.valueOf(30.00)).paymentStatus(PaymentStatus.PENDING).paymentDate(LocalDateTime.now()).build());


        // Simuler SecurityUtils.getCurrentUserId() pour retourner TEST_ADMIN_ID
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_ADMIN_ID);

            // 2. Exécuter la requête GET
            mockMvc.perform(get("/api/payments/admin")
                            .with(user(TEST_ADMIN_USERNAME).roles(TEST_ADMIN_ROLE))) // Simuler un utilisateur admin authentifié
                    // 3. Vérifier la réponse
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2)) // Deux paiements pour TEST_ADMIN_ID
                    .andExpect(jsonPath("$[0].adminId").value(TEST_ADMIN_ID))
                    .andExpect(jsonPath("$[1].adminId").value(TEST_ADMIN_ID));
        }
    }
    @Test
    void getPaymentsByOrderId_Admin_ShouldReturnPayment()throws Exception{
        // 1. Préparer les données dans la DB
        // Ce test est pour l'endpoint GET /api/payments/admin/orderId/{orderId}
        Long orderId = 300L;
        // 1. Préparer les données dans la DB
        Payment payment = Payment.builder()
                .userId(10L)
                .orderId(orderId)
                .adminId(TEST_ADMIN_ID) // Assurez-vous que l'adminId correspond à celui qui fera la requête
                .transactionId("txn_order_300")
                .paymentMethod(PaymentMethod.PAYPAL)
                .amount(BigDecimal.valueOf(120.00))
                .paymentStatus(PaymentStatus.COMPLETED)
                .paymentDate(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // Paiement pour une autre commande ou un autre admin (ne devrait pas être retourné)
        paymentRepository.save(Payment.builder().userId(12L).orderId(301L).adminId(TEST_ADMIN_ID).transactionId("txn_order_301").paymentMethod(PaymentMethod.BANK_CARD).amount(BigDecimal.valueOf(30.00)).paymentStatus(PaymentStatus.PENDING).paymentDate(LocalDateTime.now()).build());
        paymentRepository.save(Payment.builder().userId(13L).orderId(302L).adminId(999L).transactionId("txn_order_302").paymentMethod(PaymentMethod.BANK_CARD).amount(BigDecimal.valueOf(40.00)).paymentStatus(PaymentStatus.PENDING).paymentDate(LocalDateTime.now()).build());


        // Simuler SecurityUtils.getCurrentUserId() pour retourner TEST_ADMIN_ID
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_ADMIN_ID);

            // 2. Exécuter la requête GET
            mockMvc.perform(get("/api/payments/admin/orderId/{orderId}", orderId)
                            .with(user(TEST_ADMIN_USERNAME).roles(TEST_ADMIN_ROLE))) // Simuler un utilisateur admin authentifié
                    // 3. Vérifier la réponse
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(payment.getId()))
                    .andExpect(jsonPath("$.userId").value(payment.getUserId()))
                    .andExpect(jsonPath("$.adminId").value(payment.getAdminId()))
                    .andExpect(jsonPath("$.orderId").value(payment.getOrderId()))
                    .andExpect(jsonPath("$.transactionId").value(payment.getTransactionId()))
                    .andExpect(jsonPath("$.amount").value(payment.getAmount().doubleValue()))
                    .andExpect(jsonPath("$.paymentStatus").value(payment.getPaymentStatus().name()))
                    .andExpect(jsonPath("$.paymentMethod").value(payment.getPaymentMethod().name()));
        }
    }
    @Test
    void getPaymentsByOrderId_Admin_ShouldReturnNotFoundWhenNoPaymentsFound()throws Exception{
        Long nonExistentOrderId = 999L;
        // Simuler SecurityUtils.getCurrentUserId() pour retourner TEST_ADMIN_ID
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_ADMIN_ID);

            mockMvc.perform(get("/api/payments/admin/orderId/{orderId}", nonExistentOrderId)
                            .with(user(TEST_ADMIN_USERNAME).roles(TEST_ADMIN_ROLE))) // Simuler un utilisateur admin authentifié
                    .andExpect(status().isNotFound()); // Attendre un 404 Not Found
        }
    }
}
