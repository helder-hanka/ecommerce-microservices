package com.ff.paiements_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ff.paiements_service.controller.PaymentUserController;
import com.ff.paiements_service.dto.PaymentPostRequest;
import com.ff.paiements_service.entity.Payment;
import com.ff.paiements_service.entity.PaymentMethod;
import com.ff.paiements_service.entity.PaymentStatus;
import com.ff.paiements_service.service.PaymentService;
import com.ff.paiements_service.service.UserPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(PaymentUserController.class)
@Import(PaymentUserControllerTest.MockServiceConfig.class)
public class PaymentUserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserPaymentService userPaymentService;

    @Autowired
    private PaymentService paymentService;

    private Payment mockPayment;

    @TestConfiguration
    static class MockServiceConfig {
        @Bean
        public UserPaymentService userPaymentService() {
            return Mockito.mock(UserPaymentService.class);
        }

        @Bean
        public PaymentService paymentService() {
            return Mockito.mock(PaymentService.class);
        }
    }

    @BeforeEach
    void setUp() {
        mockPayment = Payment.builder()
                .id(1L)
                .userId(1L)
                .orderId(101L)
                .paymentMethod(PaymentMethod.BANK_CARD)
                .amount(BigDecimal.valueOf(100.00))
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .build();
    }

    @Test
    void createPayment_ShouldReturnPayment() throws Exception {
        PaymentPostRequest request = new PaymentPostRequest();
        request.setUserId(1L);
        request.setOrderId(101L);
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setPaymentMethod(PaymentMethod.BANK_CARD);

        // Mock du service
        Mockito.when(userPaymentService.createPayment(any())).thenReturn(mockPayment);

        mockMvc.perform(post("/api/user/payments/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockPayment.getId()))
                .andExpect(jsonPath("$.userId").value(mockPayment.getUserId()))
                .andExpect(jsonPath("$.orderId").value(mockPayment.getOrderId()))
                .andExpect(jsonPath("$.amount").value(mockPayment.getAmount().doubleValue()))
                .andExpect(jsonPath("$.paymentStatus").value(mockPayment.getPaymentStatus().name()))
                .andExpect(jsonPath("$.paymentMethod").value(mockPayment.getPaymentMethod().name()));
    }
    @Test
    void getUserPayments_ShouldReturnPayment() throws Exception {
        Long userId = 1L;
        when(paymentService.getAllPaymentsByUserId(userId)).thenReturn(List.of(mockPayment));

        mockMvc.perform(get("/api/user/payments/order/user/{userId}/all", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(mockPayment.getId()))
                .andExpect(jsonPath("$[0].userId").value(mockPayment.getUserId()))
                .andExpect(jsonPath("$[0].orderId").value(mockPayment.getOrderId()))
                .andExpect(jsonPath("$[0].amount").value(mockPayment.getAmount().doubleValue()))
                .andExpect(jsonPath("$[0].paymentStatus").value(mockPayment.getPaymentStatus().name()))
                .andExpect(jsonPath("$[0].paymentMethod").value(mockPayment.getPaymentMethod().name()));
    }
    @Test
    void getPaymentsByOrderId_ShouldReturnPayments() throws Exception {
        Long orderId = 101L;
        when(paymentService.getPaymentsByOrderId(orderId)).thenReturn(List.of(mockPayment));

        mockMvc.perform(get("/api/user/payments/order/{orderId}/all", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(mockPayment.getId()))
                .andExpect(jsonPath("$[0].userId").value(mockPayment.getUserId()))
                .andExpect(jsonPath("$[0].orderId").value(mockPayment.getOrderId()))
                .andExpect(jsonPath("$[0].amount").value(mockPayment.getAmount().doubleValue()))
                .andExpect(jsonPath("$[0].paymentStatus").value(mockPayment.getPaymentStatus().name()))
                .andExpect(jsonPath("$[0].paymentMethod").value(mockPayment.getPaymentMethod().name()));
    }
    @Test
    void getPaymentById_ShouldReturnPayment() throws Exception {
        Long paymentId = 1L;
        when(paymentService.findPaymentById(paymentId)).thenReturn(mockPayment);

        mockMvc.perform(get("/api/user/payments/order/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockPayment.getId()))
                .andExpect(jsonPath("$.userId").value(mockPayment.getUserId()))
                .andExpect(jsonPath("$.orderId").value(mockPayment.getOrderId()))
                .andExpect(jsonPath("$.amount").value(mockPayment.getAmount().doubleValue()))
                .andExpect(jsonPath("$.paymentStatus").value(mockPayment.getPaymentStatus().name()))
                .andExpect(jsonPath("$.paymentMethod").value(mockPayment.getPaymentMethod().name()));
    }
    @Test
    void createPayment_InvalidAmount_ShouldReturnBadRequest() throws Exception {
        PaymentPostRequest request = new PaymentPostRequest();
        request.setUserId(1L);
        request.setOrderId(101L);
        request.setAmount(BigDecimal.valueOf(0)); // Montant invalide
        request.setPaymentMethod(PaymentMethod.BANK_CARD);

        mockMvc.perform(post("/api/user/payments/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void createPayment_InvalidPaymentMethod_ShouldReturnBadRequest() throws Exception {
        PaymentPostRequest request = new PaymentPostRequest();
        request.setUserId(1L);
        request.setOrderId(101L);
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setPaymentMethod(null); // Méthode de paiement invalide

        mockMvc.perform(post("/api/user/payments/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
